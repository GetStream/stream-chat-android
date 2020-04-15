package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap


/**
 * The Channel Repo exposes convenient livedata objects to build your chat interface
 * It automatically handles the incoming events and keeps users, messages, reactions, channel information up to date automatically
 * Offline storage is also handled using Room
 *
 * The most commonly used livedata objects are
 *
 * - channelRepo.messages (the livedata for the list of messages)
 * - channelRepo.channel (livedata object with the channel name, image, members etc.)
 * - channelRepo.members (livedata object with the members of this channel)
 * - channelRepo.watchers (the people currently watching this channel)
 * - channelRepo.messageAndReads (interleaved list of messages and how far users have read)
 *
 * It also enables you to modify the channel. Operations will first be stored in offline storage before syncing to the server
 * - channelRepo.sendMessage stores the message locally and sends it when network is available
 * - channelRepo.sendReaction stores the reaction locally and sends it when network is available
 *
 */
class ChannelController(var channelType: String, var channelId: String, var client: ChatClient, var domain: io.getstream.chat.android.livedata.ChatDomain) {

    private val _endOfNewerMessages = MutableLiveData<Boolean>(false)
    val endOfNewerMessages: LiveData<Boolean> = _endOfNewerMessages

    private val _endOfOlderMessages = MutableLiveData<Boolean>(false)
    val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

    var recoveryNeeded: Boolean = false
    private var lastMarkReadEvent: Date? = null
    private var lastKeystrokeAt: Date? = null
    private var lastStartTypingEvent: Date? = null
    val channelController = client.channel(channelType, channelId)
    val cid = "%s:%s".format(channelType, channelId)

    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + domain.job + job)

    private val logger = ChatLogger.get("ChannelRepo")

    private val _messages = MutableLiveData<MutableMap<String, Message>>()
    /** LiveData object with the messages */

    // TODO 1.1: we could make this more efficient by using a data structure that keeps the sort
    val messages: LiveData<List<Message>> = Transformations.map(_messages) {
        it.values.sortedBy { it.createdAt }
    }

    private val _channel = MutableLiveData<Channel>()
    /** LiveData object with the channel information (members, data etc.) */
    val channel: LiveData<Channel> = _channel

    private val _watcherCount = MutableLiveData<Int>()
    val watcherCount: LiveData<Int> = _watcherCount

    private val _typing = MutableLiveData<MutableMap<String, ChatEvent>>()
    val typing: LiveData<List<User>> = Transformations.map(_typing) {
        it.values.sortedBy { it.receivedAt }.map { it.user!! }
    }

    private val _reads = MutableLiveData<MutableMap<String, ChannelUserRead>>()
    val reads: LiveData<List<ChannelUserRead>> = Transformations.map(_reads) {
        it.values.sortedBy { it.lastRead }
    }

    private val _read = MutableLiveData<ChannelUserRead>()
    val read: LiveData<ChannelUserRead> = _read


    private val _unreadCount = MutableLiveData<Int>()
    /**
     * unread count for this channel
     */
    val unreadCount: LiveData<Int> = ChannelUnreadCountLiveData(domain.currentUser, read, messages)

    private val _watchers = MutableLiveData<MutableMap<String, User>>()
    val watchers: LiveData<List<User>> = Transformations.map(_watchers) {
        it.values.sortedBy { it.createdAt }
    }

    private val _members = MutableLiveData<MutableMap<String, Member>>()
    val members: LiveData<List<Member>> = Transformations.map(_members) {
        it.values.sortedBy { it.createdAt }
    }

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages

    private val _loadingNewerMessages = MutableLiveData<Boolean>(false)
    val loadingNewerMessages: LiveData<Boolean> = _loadingNewerMessages


    val _threads: MutableMap<String, MutableLiveData<MutableMap<String, Message>>> = mutableMapOf()

    fun getThreadMessages(threadId: String): LiveData<List<Message>> {
        val threadMessageMap = _threads.getOrElse(threadId) { MutableLiveData(mutableMapOf()) }
        return Transformations.map(threadMessageMap) { it.values.sortedBy { m -> m.createdAt } }
    }

    fun getThread(threadId: String): ThreadController {
        if (!activeThreadMap.containsKey(threadId)) {
            val channelRepo = ThreadController(threadId, this)
            activeThreadMap.put(threadId, channelRepo)
        }
        return activeThreadMap.getValue(threadId)
    }



    fun getConfig(): Config {
        return domain.getChannelConfig(channelType)
    }

    fun keystroke(): Result<Boolean> {
        if (!getConfig().isTypingEvents) return Result(false, null)
        lastKeystrokeAt = Date()
        if (lastStartTypingEvent == null || lastKeystrokeAt!!.time - lastStartTypingEvent!!.time > 3000) {
            lastStartTypingEvent = lastKeystrokeAt
            val result = client.sendEvent(EventType.TYPING_START, channelType, channelId).execute()
            return if (result.isSuccess) {
                Result(result.isSuccess, null)
            } else {
                Result(result.isSuccess, null)
            }
        }
        return return Result(false, null)
    }


    fun stopTyping(): Result<Boolean> {
        if (!getConfig().isTypingEvents) return Result(false, null)
        if (lastStartTypingEvent != null) {
            lastStartTypingEvent = null
            lastKeystrokeAt = null
            val result = client.sendEvent(EventType.TYPING_STOP, channelType, channelId).execute()
            return Result(result.isSuccess, result.error())
        }
        return Result(false, null)
    }


    fun markRead(): Boolean {
        if (!getConfig().isReadEvents) return false
        // throttle the mark read
        val messages = sortedMessages()
        if (messages.isNotEmpty()) {
            val last = messages.last()
            val lastMessageDate = last.createdAt

            if (lastMarkReadEvent == null || lastMessageDate!!.after(lastMarkReadEvent)) {
                lastMarkReadEvent = lastMessageDate
                val userRead = ChannelUserRead().apply { user = domain.currentUser; lastRead = last.createdAt }
                _read.postValue(userRead)
                client.markMessageRead(channelType, channelId, last.id).execute()
                return true
            }
        }
        return false
    }


    fun loadNewerMessages(limit: Int = 30) {
        GlobalScope.launch(Dispatchers.IO) {
            _loadNewerMessages(limit)
        }
    }

    /** stores the mapping from cid to channelRepository */
    var activeThreadMap: ConcurrentHashMap<String, ThreadController> = ConcurrentHashMap()


    fun sortedMessages(): List<Message> {
        // sorted ascending order, so the oldest messages are at the beginning of the list
        val messageMap = _messages.value ?: mutableMapOf()
        return messageMap.values.sortedBy { it.createdAt }
    }

    fun threadLoadOlderMessages(threadId: String, limit: Int = 30) {
        GlobalScope.launch(Dispatchers.IO) {
            loadMoreThreadMessages(threadId, limit, Pagination.LESS_THAN)
        }
    }

    // TODO: test me
    suspend fun loadMoreThreadMessages(threadId: String, limit: Int = 30, direction: Pagination): Result<List<Message>> {
        val thread = getThreadMessages(threadId)

        val threadMessages = thread.value ?: emptyList()

        var getRepliesCall: Call<List<Message>>
        if (threadMessages.isNotEmpty()) {
            val messageId: String = when (direction) {
                Pagination.GREATER_THAN_OR_EQUAL, Pagination.GREATER_THAN -> {
                    threadMessages.last().id
                }
                Pagination.LESS_THAN, Pagination.LESS_THAN_OR_EQUAL -> {
                    threadMessages.first().id
                }
            }

            getRepliesCall = client.getRepliesMore(threadId, messageId, limit)
        } else {
            getRepliesCall = client.getReplies(threadId, limit)
        }

        val response = getRepliesCall.execute()
        if (response.isSuccess) {
            upsertMessages(response.data())
        } else {
            domain.addError(response.error())
        }
        return response


    }


    fun watch(limit: Int = 30) {
        scope.launch(Dispatchers.IO) {
            _watch(limit)
        }
    }

    suspend fun _watch(limit: Int = 30) {
        // Otherwise it's too easy for devs to create UI bugs which DDOS our API
        if (_loading.value == true) {
            logger.logI("Another request to watch this channel is in progress. Ignoring this request.")
            return
        }
        _loading.postValue(true)
        val pagination = QueryChannelPaginationRequest(limit)
        runChannelQuery(pagination)

        _loading.postValue(false)
    }

    fun loadMoreMessagesRequest(limit: Int = 30, direction: Pagination): QueryChannelPaginationRequest {
        val messages = sortedMessages()
        var request = QueryChannelPaginationRequest(limit)
        if (messages.isNotEmpty()) {
            val messageId: String = when (direction) {
                Pagination.GREATER_THAN_OR_EQUAL, Pagination.GREATER_THAN -> {
                    messages.last().id
                }
                Pagination.LESS_THAN, Pagination.LESS_THAN_OR_EQUAL -> {
                    messages.first().id
                }
            }
            request = request.apply { messageFilterDirection = direction; messageFilterValue = messageId }

        }

        return request
    }


    suspend fun loadOlderMessages(limit: Int = 30) {
        if (_loadingOlderMessages.value == true) {
            logger.logI("Another request to load older messages is in progress. Ignoring this request.")
            return
        }
        _loadingOlderMessages.postValue(true)
        val pagination = loadMoreMessagesRequest(limit, Pagination.LESS_THAN)
        runChannelQuery(pagination)
        _loadingOlderMessages.postValue(false)
    }

    suspend fun _loadNewerMessages(limit: Int = 30) {
        if (_loadingNewerMessages.value == true) {
            logger.logI("Another request to load newer messages is in progress. Ignoring this request.")
            return
        }
        _loadingNewerMessages.value = true
        val pagination = loadMoreMessagesRequest(limit, Pagination.GREATER_THAN)
        runChannelQuery(pagination)
        _loadingNewerMessages.value = false
    }

    suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest) {
        // first we load the data from room and update the messages and channel livedata
        runChannelQueryOffline(pagination)

        // if we are online we we run the actual API call
        if (domain.isOnline()) {

            runChannelQueryOnline(pagination)
        } else {
            // if we are not offline we mark it as needing recovery
            recoveryNeeded = true
        }
    }

    suspend fun runChannelQueryOffline(pagination: QueryChannelPaginationRequest) {
        val channel = domain.selectAndEnrichChannel(cid, pagination)

        channel?.let {
            it.config = domain.getChannelConfig(it.type)
            _loading.postValue(false)
            if (it.messages.isNotEmpty()) {
                upsertMessages(it.messages)
            }
            logger.logI("Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages")

        }

        // for pagination we cant use channel.messages, so discourage that
        if (channel != null) {
            // TODO: having a channel data concept which only has a subset of the channel fields would prevent coding errors
            channel.messages = emptyList()
            _channel.postValue(channel)
        }
    }

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest) {
        val request = pagination.toQueryChannelRequest(domain.userPresence)
        val response = channelController.watch(request).execute()

        if (response.isSuccess) {
            recoveryNeeded = false
            val channelResponse = response.data()
            if (pagination.messageLimit > channelResponse.messages.size) {
                if (request.isFilteringNewerMessages()) {
                    _endOfNewerMessages.postValue(true)
                } else {
                    _endOfOlderMessages.postValue(true)
                }
            }
            // first thing here needs to be updating configs otherwise we have a race with receiving events
            val configEntities = ChannelConfigEntity(channelResponse.type, channelResponse.config)
            domain.repos.configs.insert(listOf(configEntities))
            updateLiveDataFromChannel(channelResponse)
            domain.storeStateForChannel(channelResponse)

        } else {
            recoveryNeeded = true
            domain.addError(response.error())
        }
    }

    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendMessage(message: Message): Result<Message> {
        var result : Result<Message>
        // set defaults for id, cid and created at
        if (message.id.isEmpty()) {
            message.id = domain.generateMessageId()
        }
        if (message.cid.isEmpty()) {
            message.cid = cid
        }
        val channel = checkNotNull(_channel.value) { "Channel needs to be set before sending a message" }
        message.channel = channel
        message.user = domain.currentUser
        message.createdAt = message.createdAt ?: Date()
        message.syncStatus = SyncStatus.SYNC_NEEDED

        val messageEntity = MessageEntity(message)

        // Update livedata
        upsertMessage(message)
        setLastMessage(message)

        // Update Room State
        domain.repos.messages.insertMessage(message)

        val channelStateEntity = domain.repos.channels.select(message.channel.cid)
        channelStateEntity?.let {
            // update channel lastMessage at and lastMessageAt
            it.addMessage(messageEntity)
            domain.repos.channels.insert(it)
        }

        if (domain.isOnline()) {
            val runnable = {
                val result = channelController.sendMessage(message)
                result as Call<Any>
            }
            val result = domain.runAndRetry(runnable)
            if (result.isSuccess) {
                // set sendMessageCompletedAt so we know when to edit vs call sendMessage
                messageEntity.syncStatus = SyncStatus.SYNCED
                messageEntity.sendMessageCompletedAt = Date()
                domain.repos.messages.insert(messageEntity)
            }
            return Result(result.data() as Message?, result.error())
        }
        // TODO: indicate that we are offline
        return Result(message, null)
    }

    private fun setLastMessage(message: Message) {
        val copy = _channel.value!!
        copy.lastMessageAt = message.createdAt
        _channel.postValue(copy)
    }


    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendReaction(reaction: Reaction): Result<Reaction> {
        reaction.user = domain.currentUser
        // insert the message into local storage
        val reactionEntity = ReactionEntity(reaction)
        reactionEntity.syncStatus = SyncStatus.SYNC_NEEDED
        domain.repos.reactions.insert(reactionEntity)
        // update livedata
        val currentMessage = getMessage(reaction.messageId)
        currentMessage?.let {
            it.ownReactions.add(reaction)
            it.latestReactions.add(reaction)
            upsertMessage(it)
        }
        // update the message in the local storage
        val messageEntity = domain.repos.messages.selectMessageEntity(reaction.messageId)
        messageEntity?.let {
            it.addReaction(reaction, domain.currentUser.id == reaction.user!!.id)
            domain.repos.messages.insert(it)
        }
        val online = domain.isOnline()
        if (online) {
            val runnable = {
                client.sendReaction(reaction) as Call<Any>
            }
            val result = domain.runAndRetry(runnable)
            if (result.isSuccess) {
                return Result(result.data() as Reaction, result.error())
            } else {
                return Result(null, result.error())
            }
        }
        return Result(reaction, null)
    }


    suspend fun deleteReaction(reaction: Reaction): Result<Reaction> {
        reaction.user = domain.currentUser
        reaction.syncStatus = SyncStatus.SYNC_NEEDED

        val reactionEntity = ReactionEntity(reaction)
        reactionEntity.deletedAt = Date()
        reactionEntity.syncStatus = SyncStatus.SYNC_NEEDED
        domain.repos.reactions.insert(reactionEntity)

        // update livedata
        val currentMessage = getMessage(reaction.messageId)
        currentMessage?.let {
            it.ownReactions = it.ownReactions.filter { it.user!!.id == reaction.userId && it.type == reaction.type }.toMutableList()
            it.latestReactions = it.latestReactions.filter { it.user!!.id == reaction.userId && it.type == reaction.type }.toMutableList()
            upsertMessage(it)
        }

        val messageEntity = domain.repos.messages.selectMessageEntity(reaction.messageId)
        messageEntity?.let {
            it.removeReaction(reaction, domain.currentUser.id == reaction.user!!.id)
            domain.repos.messages.insert(it)
        }
        val online = domain.isOnline()
        if (online) {
            val runnable = {
                client.deleteReaction(reaction.messageId, reaction.type) as Call<Any>
            }
            val result = domain.runAndRetry(runnable)
            if (result.isSuccess) {
                return Result(result.data() as Reaction, null)
            } else {
                return Result(null, result.error())
            }
        }
        return Result(reaction, null)
    }

    fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.postValue(watcherCount)
        }
    }

    // This one needs to be public for flows such as running a message action
    fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    fun upsertEventMessage(message: Message) {
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }
        upsertMessages(listOf(message))
    }

    fun getMessage(messageId: String): Message? {
        val copy = _messages.value ?: mutableMapOf()
        val message = copy.get(messageId)
        return message
    }

    fun upsertMessages(messages: List<Message>) {
        val copy = _messages.value ?: mutableMapOf()
        for (message in messages) {
            copy[message.id] = message
            // handle threads
            val parentId = message.parentId ?: ""
            if (!parentId.isEmpty()) {
                var threadMessages = mutableMapOf<String, Message>()
                if (_threads.containsKey(parentId)) {
                    threadMessages = _threads[parentId]!!.value!!
                } else {
                    val parent = getMessage(parentId)
                    parent?.let { threadMessages.set(it.id, it) }
                }
                threadMessages.set(message.id, message)
                _threads[parentId] = MutableLiveData(threadMessages)
            }
        }
        _messages.postValue(copy)
    }

    fun clean() {
        // cleanup your own typing state
        val now = Date()
        if (lastStartTypingEvent != null && now.time - lastStartTypingEvent!!.time > 5000) {
            stopTyping()
        }

        // Cleanup typing events that are older than 15 seconds
        val copy = _typing.value ?: mutableMapOf()
        var changed = false
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, -15)
        val old = calendar.time
        for ((userId, typing) in copy.toList()) {
            if (typing.receivedAt.before(old)) {
                copy.remove(userId)
                changed = true
            }
        }
        if (changed) {
            _typing.postValue(copy)
        }
    }

    fun setTyping(userId: String, event: ChatEvent?) {
        val copy = _typing.value ?: mutableMapOf()
        if (event == null) {
            copy.remove(userId)
        } else {
            copy[userId] = event
        }
        _typing.postValue(copy)
    }

    fun handleEvents(events: List<ChatEvent>) {
        // livedata actually batches many frequent updates after each other
        // we might not need a more optimized handleEvents implementation.. TBD.
        for (event in events) {
            handleEvent(event)
        }
    }

    fun handleEvent(event: ChatEvent) {
        event.channel?.watcherCount?.let {
            setWatcherCount(it)
        }
        when (event) {
            is NewMessageEvent, is MessageUpdatedEvent, is MessageDeletedEvent -> {
                upsertEventMessage(event.message)
            }
            is ReactionNewEvent, is ReactionDeletedEvent -> {
                upsertEventMessage(event.message)
            }
            is MemberRemovedEvent -> {
                deleteMember(event.member!!)
            }
            is MemberAddedEvent, is MemberUpdatedEvent, is NotificationAddedToChannelEvent -> {
                // add /remove the members etc
                upsertMember(event.member!!)
            }

            is UserPresenceChanged, is UserUpdated -> {
                upsertUser(event.user)
            }

            is UserStartWatchingEvent -> {
                upsertWatcher(event.user!!)
            }
            is UserStopWatchingEvent -> {
                deleteWatcher(event.user!!)
            }
            is ChannelUpdatedEvent -> {
                // TODO: this shouldn't update members and watchers since we can have more than 100 of those and they won't be in the return object
                event.channel?.let { updateChannel(it) }
            }
            is TypingStopEvent -> {
                setTyping(event.user?.id!!, null)
            }
            is TypingStartEvent -> {
                setTyping(event.user?.id!!, event)
            }
            is MessageReadEvent, is NotificationMarkReadEvent -> {
                val read = ChannelUserRead().apply { user = event.user!!; lastRead = event.createdAt!! }
                updateRead(read)
            }
        }
    }

    private fun upsertUser(user: User?) {
        // TODO: 1.1 add support for updating users
    }

    private fun deleteWatcher(user: User) {
        val copy = _watchers.value ?: mutableMapOf()
        copy.remove(user.id)
        _watchers.postValue(copy)
    }

    private fun upsertWatcher(user: User) {
        val copy = _watchers.value ?: mutableMapOf()
        copy[user.id] = user
        _watchers.postValue(copy)
    }

    private fun deleteMember(member: Member) {
        val copy = _members.value ?: mutableMapOf()
        copy.remove(member.user.id)
        _members.postValue(copy)
    }

    fun upsertMember(member: Member) {
        val copy = _members.value ?: mutableMapOf()
        copy[member.user.id] = member
        _members.postValue(copy)
    }

    fun updateReads(
            reads: List<ChannelUserRead>
    ) {
        val currentUser = domain.currentUser
        val copy = _reads.value ?: mutableMapOf()
        for (r in reads) {
            copy[r.getUserId()] = r
            // update read state for the current user
            if (r.getUserId() == currentUser.id) {
                _read.postValue(r)
            }
        }
        _reads.postValue(copy)
    }

    fun updateRead(
            read: ChannelUserRead
    ) {
        updateReads(listOf(read))
    }

    suspend fun updateLiveDataFromChannel(c: Channel) {
        // Update all the livedata objects based on the channel
        // TODO: there are some issues here when you have more than 100 members, watchers

        updateChannel(c)
        setMembers(c.members)
        setWatchers(c.watchers)
        setWatcherCount(c.watcherCount)
        updateReads(c.read)
        upsertMessages(c.messages)


    }

    private fun setMembers(members: List<Member>) {
        val copy = _members.value ?: mutableMapOf()
        for (m in members) {
            copy[m.getUserId()] = m
        }
        _members.postValue(copy)
    }

    fun updateChannel(channel: Channel) {
        _channel.postValue(channel)
    }

    fun setWatchers(watchers: List<Watcher>) {
        val copy = _watchers.value ?: mutableMapOf()
        for (watcher in watchers) {
            watcher.user?.let {
                copy[it.id] = it
            }
        }

        _watchers.postValue(copy)
    }

    suspend fun editMessage(message: Message): Result<Message> {
        message.updatedAt = Date()
        message.syncStatus = SyncStatus.SYNC_NEEDED

        // Update livedata
        upsertMessage(message)

        // Update Room State
        domain.repos.messages.insertMessage(message)

        if (domain.isOnline()) {
            val runnable = {
                client.updateMessage(message) as Call<Any>
            }
            val result = domain.runAndRetry(runnable)
            return Result(result.data() as Message, result.error())
        }
        return Result(message, null)
    }


    suspend fun deleteMessage(message: Message): Result<Message> {
        message.deletedAt = Date()
        message.syncStatus = SyncStatus.SYNC_NEEDED

        // Update livedata
        upsertMessage(message)

        // Update Room State
        domain.repos.messages.insertMessage(message)

        if (domain.isOnline()) {
            val runnable = {
                client.deleteMessage(message.id) as Call<Any>
            }
            val result = domain.runAndRetry(runnable)
            return Result(result.data() as Message, result.error())
        }
        return Result(message, null)
    }

    fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        val channel = _channel.value
                ?: Channel().apply { this.type = channelType; this.id = channelId; this.cid = "${channelType}:${channelId}" }
        val messages = sortedMessages()
        val members = (_members.value ?: mutableMapOf()).values.toList()
        val watchers = (_watchers.value ?: mutableMapOf()).values.toList()
        val reads = (_reads.value ?: mutableMapOf()).values.toList()
        channel.messages = messages
        channel.members = members
        channel.config = getConfig()
        // TODO: we should clearly store watchers, event system is weird though
        channel.watchers = watchers.map { Watcher(it.id).apply { user = it } }
        channel.read = reads
        return channel
    }


}