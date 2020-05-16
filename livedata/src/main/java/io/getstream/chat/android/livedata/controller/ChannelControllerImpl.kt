package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.ChannelData
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.addReaction
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.ChannelEntityPair
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import io.getstream.chat.android.livedata.isPermanent
import io.getstream.chat.android.livedata.removeReaction
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.livedata.utils.ChannelUnreadCountLiveData
import io.getstream.chat.android.livedata.utils.computeUnreadCount
import java.util.Calendar
import java.util.Date
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.withContext

class ChannelControllerImpl(
    override var channelType: String,
    override var channelId: String,
    var client: ChatClient,
    var domainImpl: ChatDomainImpl
) :
    ChannelController {
    private val _messages = MutableLiveData<MutableMap<String, Message>>()
    private val _watcherCount = MutableLiveData<Int>()
    private val _typing = MutableLiveData<MutableMap<String, ChatEvent>>()
    private val _reads = MutableLiveData<MutableMap<String, ChannelUserRead>>()
    private val _read = MutableLiveData<ChannelUserRead>()
    private val _endOfNewerMessages = MutableLiveData<Boolean>(false)
    private val _endOfOlderMessages = MutableLiveData<Boolean>(false)
    private val _loading = MutableLiveData<Boolean>(false)
    private val _hidden = MutableLiveData<Boolean>(false)
    private val _muted = MutableLiveData<Boolean>(false)
    private val _watchers = MutableLiveData<MutableMap<String, User>>()
    private val _members = MutableLiveData<MutableMap<String, Member>>()
    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    private val _loadingNewerMessages = MutableLiveData<Boolean>(false)
    private val _channelData = MutableLiveData<ChannelData>()
    internal var hideMessagesBefore: Date? = null

    /** a list of messages sorted by message.createdAt */
    override val messages: LiveData<List<Message>> = Transformations.map(_messages) {
        // TODO: consider removing this check
        it.values.sortedBy { it.createdAt }.filter { hideMessagesBefore == null || it.createdAt!! > hideMessagesBefore }
    }

    /** the number of people currently watching the channel */
    override val watcherCount: LiveData<Int> = _watcherCount
    /** the list of users currently watching this channel */
    override val watchers: LiveData<List<User>> = Transformations.map(_watchers) {
        it.values.sortedBy { it.createdAt }
    }

    /** who is currently typing (current user is excluded from this) */
    override val typing: LiveData<List<User>> = Transformations.map(_typing) {
        it.values.sortedBy { it.receivedAt }.map { it.user!! }
    }

    /** how far every user in this channel has read */
    override val reads: LiveData<List<ChannelUserRead>> = Transformations.map(_reads) {
        it.values.sortedBy { it.lastRead }
    }

    /** read status for the current user */
    override val read: LiveData<ChannelUserRead> = _read

    /**
     * unread count for this channel, calculated based on read state (this works even if you're offline)
     */
    override val unreadCount: LiveData<Int> =
        ChannelUnreadCountLiveData(
            domainImpl.currentUser,
            read,
            messages
        )

    /** the list of members of this channel */
    override val members: LiveData<List<Member>> = Transformations.map(_members) {
        it.values.sortedBy { it.createdAt }
    }

    /** LiveData object with the channel data */
    override val channelData: LiveData<ChannelData> = _channelData

    /** if the channel is currently hidden */
    override val hidden: LiveData<Boolean> = _hidden

    /** if the channel is currently muted */
    override val muted: LiveData<Boolean> = _muted

    /** if we are currently loading */
    override val loading: LiveData<Boolean> = _loading

    /** if we are currently loading older messages */
    override val loadingOlderMessages: LiveData<Boolean> = _loadingOlderMessages

    /** if we are currently loading newer messages */
    override val loadingNewerMessages: LiveData<Boolean> = _loadingNewerMessages

    /** set to true if there are no more older messages to load */
    override val endOfOlderMessages: LiveData<Boolean> = _endOfOlderMessages

    /** set to true if there are no more newer messages to load */
    override val endOfNewerMessages: LiveData<Boolean> = _endOfNewerMessages

    override var recoveryNeeded: Boolean = false
    private var lastMarkReadEvent: Date? = null
    private var lastKeystrokeAt: Date? = null
    private var lastStartTypingEvent: Date? = null
    val channelController = client.channel(channelType, channelId)
    override val cid = "%s:%s".format(channelType, channelId)

    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + domainImpl.job + job)

    private val logger = ChatLogger.get("ChatDomain ChannelController")

    private val _threads: MutableMap<String, MutableLiveData<MutableMap<String, Message>>> = mutableMapOf()

    fun getThreadMessages(threadId: String): MutableLiveData<MutableMap<String, Message>> {
        val threadMessageMap = _threads.getOrElse(threadId) { MutableLiveData(mutableMapOf()) }
        return threadMessageMap
    }

    fun getThread(threadId: String): ThreadControllerImpl {
        if (!activeThreadMapImpl.containsKey(threadId)) {
            // start the thread if it doesn't exist yet
            if (!_threads.containsKey(threadId)) {
                val messagesMap = mutableMapOf<String, Message>()
                val message = getMessage(threadId)
                message?.let { messagesMap[it.id] = it }
                _threads[threadId] = MutableLiveData(messagesMap)
            }

            val threadController =
                ThreadControllerImpl(
                    threadId,
                    this
                )
            activeThreadMapImpl[threadId] = threadController
        }
        return activeThreadMapImpl.getValue(threadId)
    }

    fun getConfig(): Config {
        return domainImpl.getChannelConfig(channelType)
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
        return Result(false, null)
    }

    fun stopTyping(): Result<Boolean> {
        if (!getConfig().isTypingEvents) return Result(false, null)
        if (lastStartTypingEvent != null) {
            lastStartTypingEvent = null
            lastKeystrokeAt = null
            val result = client.sendEvent(EventType.TYPING_STOP, channelType, channelId).execute()
            return if (result.isSuccess) {
                Result(result.isSuccess, null)
            } else {
                Result(null, result.error())
            }
        }
        return Result(false, null)
    }

    fun markRead(): Result<Boolean> {
        if (!getConfig().isReadEvents) return Result(false, null)
        // throttle the mark read
        val messages = sortedMessages()
        if (messages.isNotEmpty()) {
            val last = messages.last()
            val lastMessageDate = last.createdAt

            if (lastMarkReadEvent == null || lastMessageDate!!.after(lastMarkReadEvent)) {
                lastMarkReadEvent = lastMessageDate
                val userRead = ChannelUserRead(domainImpl.currentUser).apply { lastRead = last.createdAt }
                _read.postValue(userRead)
                client.markMessageRead(channelType, channelId, last.id).execute()
                return Result(true, null)
            }
        }
        return Result(false, null)
    }

    /** stores the mapping from cid to channelRepository */
    var activeThreadMapImpl: ConcurrentHashMap<String, ThreadControllerImpl> = ConcurrentHashMap()

    fun sortedMessages(): List<Message> {
        // sorted ascending order, so the oldest messages are at the beginning of the list
        val messageMap = _messages.value ?: mutableMapOf()
        val messages = messageMap.values.sortedBy { it.createdAt }.filter { hideMessagesBefore == null || it.createdAt!! > hideMessagesBefore }
        return messages
    }

    suspend fun loadMoreThreadMessages(threadId: String, limit: Int = 30, direction: Pagination): Result<List<Message>> {
        val thread = getThreadMessages(threadId)

        val threadMessagesMap = thread.value ?: mutableMapOf()
        val threadMessages = threadMessagesMap.values.sortedBy { it.createdAt }

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
            domainImpl.addError(response.error())
        }
        return response
    }

    fun removeMessagesBefore(t: Date) {
        val copy = _messages.value ?: mutableMapOf()
        // start off empty
        _messages.postValue(mutableMapOf())
        // call upsert with the messages that are recent
        val recentMessages = copy.values.filter { it.createdAt!! > t }
        upsertMessages(recentMessages)
    }

    suspend fun hide(clearHistory: Boolean): Result<Unit> {
        setHidden(true)
        val result = channelController.hide(clearHistory).execute()
        if (result.isSuccess) {
            val channelEntity = domainImpl.repos.channels.select(cid)
            channelEntity?.let {
                it.hidden = true
                if (clearHistory) {
                    val now = Date()
                    it.hideMessagesBefore = now
                    hideMessagesBefore = now
                    removeMessagesBefore(now)
                    domainImpl.repos.messages.deleteChannelMessagesBefore(cid, now)
                }
                domainImpl.repos.channels.insert(it)
            }
        }
        return result
    }

    suspend fun show(): Result<Unit> {
        setHidden(false)
        val result = channelController.show().execute()
        if (result.isSuccess) {
            val channelEntity = domainImpl.repos.channels.select(cid)
            channelEntity?.let {
                it.hidden = false
                domainImpl.repos.channels.insert(it)
            }
        }
        return result
    }

    suspend fun watch(limit: Int = 30) {
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

    suspend fun loadOlderMessages(limit: Int = 30): Result<Channel> {
        if (_loadingOlderMessages.value == true) {
            logger.logI("Another request to load older messages is in progress. Ignoring this request.")
            return Result(null, ChatError("Another request to load older messages is in progress. Ignoring this request."))
        }
        _loadingOlderMessages.postValue(true)
        val pagination = loadMoreMessagesRequest(limit, Pagination.LESS_THAN)
        val result = runChannelQuery(pagination)
        _loadingOlderMessages.postValue(false)
        return result
    }

    suspend fun loadNewerMessages(limit: Int = 30): Result<Channel> {
        if (_loadingNewerMessages.value == true) {
            logger.logI("Another request to load newer messages is in progress. Ignoring this request.")
            return Result(null, ChatError("Another request to load newer messages is in progress. Ignoring this request."))
        }
        _loadingNewerMessages.value = true
        val pagination = loadMoreMessagesRequest(limit, Pagination.GREATER_THAN)
        val result = runChannelQuery(pagination)
        _loadingNewerMessages.value = false
        return result
    }

    suspend fun runChannelQuery(pagination: QueryChannelPaginationRequest): Result<Channel> {
        // first we load the data from room and update the messages and channel livedata
        val channel = runChannelQueryOffline(pagination)

        // if we are online we we run the actual API call

        var result = if (domainImpl.isOnline()) {
            runChannelQueryOnline(pagination)
        } else {
            // if we are not offline we mark it as needing recovery
            recoveryNeeded = true
            Result(channel, null)
        }
        return result
    }

    suspend fun runChannelQueryOffline(pagination: QueryChannelPaginationRequest): Channel? {
        val channelPair = domainImpl.selectAndEnrichChannel(cid, pagination)

        channelPair?.let {
            val channel = it.channel
            it.channel.config = domainImpl.getChannelConfig(it.channel.type)
            _loading.postValue(false)

            updateLiveDataFromChannelEntityPair(it)
            logger.logI("Loaded channel ${channel.cid} from offline storage with ${channel.messages.size} messages")
        }

        return channelPair?.channel
    }

    suspend fun runChannelQueryOnline(pagination: QueryChannelPaginationRequest): Result<Channel> {
        val request = pagination.toQueryChannelRequest(domainImpl.userPresence)
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
            domainImpl.repos.configs.insert(listOf(configEntities))
            updateLiveDataFromChannel(channelResponse)
            domainImpl.storeStateForChannel(channelResponse)
        } else {
            recoveryNeeded = true
            domainImpl.addError(response.error())
        }
        return response
    }

    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */

    suspend fun sendMessage(message: Message): Result<Message> = withContext(scope.coroutineContext) {
        var output: Result<Message>
        val online = domainImpl.isOnline()

        // set defaults for id, cid and created at
        if (message.id.isEmpty()) {
            message.id = domainImpl.generateMessageId()
        }
        if (message.cid.isEmpty()) {
            message.cid = cid
        }

        message.user = domainImpl.currentUser
        message.createdAt = message.createdAt ?: Date()
        message.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            message.syncStatus = SyncStatus.SYNC_NEEDED
        }

        val messageEntity = MessageEntity(message)

        // Update livedata
        upsertMessage(message)

        // we insert early to ensure we don't lose messages
        domainImpl.repos.messages.insertMessage(message)

        val channelStateEntity = domainImpl.repos.channels.select(message.cid)
        channelStateEntity?.let {
            // update channel lastMessage at and lastMessageAt
            it.addMessage(messageEntity)
            domainImpl.repos.channels.insert(it)
        }

        if (online) {
            logger.logI("Starting to send message with id ${message.id} and text ${message.text}")

            val runnable = {
                val result = channelController.sendMessage(message)
                result
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                // set sendMessageCompletedAt so we know when to edit vs call sendMessage
                messageEntity.syncStatus = SyncStatus.COMPLETED
                messageEntity.sendMessageCompletedAt = Date()
                domainImpl.repos.messages.insert(messageEntity)
                output = Result(result.data() as Message?, null)
            } else {
                if (result.error().isPermanent()) {
                    messageEntity.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    messageEntity.syncStatus = SyncStatus.SYNC_NEEDED
                }
                domainImpl.repos.messages.insert(messageEntity)
                output = Result(null, result.error())
            }
        } else {
            logger.logI("Chat is offline, postponing send message with id ${message.id} and text ${message.text}")
            output = Result(message, null)
        }

        output
    }

    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendReaction(reaction: Reaction): Result<Reaction> {
        reaction.user = domainImpl.currentUser
        val online = domainImpl.isOnline()
        // insert the message into local storage

        reaction.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            reaction.syncStatus = SyncStatus.SYNC_NEEDED
        }
        domainImpl.repos.reactions.insertReaction(reaction)
        // update livedata
        val currentMessage = getMessage(reaction.messageId)
        currentMessage?.let {
            it.addReaction(reaction, true)
            upsertMessage(it)
        }
        // update the message in the local storage
        val messageEntity = domainImpl.repos.messages.select(reaction.messageId)
        messageEntity?.let {
            it.addReaction(reaction, domainImpl.currentUser.id == reaction.user!!.id)
            domainImpl.repos.messages.insert(it)
        }

        if (online) {
            val runnable = {
                client.sendReaction(reaction)
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.reactions.insertReaction(reaction)
                return Result(result.data() as Reaction, null)
            } else {
                if (result.error().isPermanent()) {
                    reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    reaction.syncStatus = SyncStatus.SYNC_NEEDED
                }
                domainImpl.repos.reactions.insertReaction(reaction)
                return Result(null, result.error())
            }
        }
        return Result(reaction, null)
    }

    suspend fun deleteReaction(reaction: Reaction): Result<Message> {
        val online = domainImpl.isOnline()
        reaction.user = domainImpl.currentUser
        reaction.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            reaction.syncStatus = SyncStatus.SYNC_NEEDED
        }

        val reactionEntity = ReactionEntity(reaction)
        reactionEntity.deletedAt = Date()
        domainImpl.repos.reactions.insert(reactionEntity)

        // update livedata
        val currentMessage = getMessage(reaction.messageId)
        currentMessage?.let {
            it.removeReaction(reaction, true)
            upsertMessage(it)
        }

        val messageEntity = domainImpl.repos.messages.select(reaction.messageId)
        messageEntity?.let {
            it.removeReaction(reaction, domainImpl.currentUser.id == reaction.user!!.id)
            domainImpl.repos.messages.insert(it)
        }

        if (online) {
            val runnable = {
                client.deleteReaction(reaction.messageId, reaction.type)
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                reaction.syncStatus = SyncStatus.COMPLETED
                domainImpl.repos.reactions.insertReaction(reaction)
                return Result(result.data() as Message, null)
            } else {
                if (result.error().isPermanent()) {
                    reaction.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    reaction.syncStatus = SyncStatus.SYNC_NEEDED
                }
                domainImpl.repos.reactions.insertReaction(reaction)
                return Result(null, result.error())
            }
        }
        return Result(currentMessage, null)
    }

    fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.postValue(watcherCount)
        }
    }

    // This one needs to be public for flows such as running a message action
    override fun upsertMessage(message: Message) {
        upsertMessages(listOf(message))
    }

    fun upsertEventMessage(message: Message) {
        // make sure we don't lose ownReactions
        getMessage(message.id)?.let {
            message.ownReactions = it.ownReactions
        }
        upsertMessages(listOf(message))
    }

    override fun getMessage(messageId: String): Message? {
        val copy = _messages.value ?: mutableMapOf()
        var message = copy[messageId]

        if (hideMessagesBefore != null) {
            if (message != null && message.createdAt!! <= hideMessagesBefore) {
                message = null
            }
        }
        return message
    }

    fun upsertMessages(messages: List<Message>) {
        val copy = _messages.value ?: mutableMapOf()
        // filter out old events
        val freshMessages = mutableListOf<Message>()
        for (message in messages) {
            val oldMessage = copy[message.id]
            var outdated = false
            if (oldMessage != null) {
                val oldTime = oldMessage.updatedAt?.time ?: 0
                val newTime = message.updatedAt?.time ?: 0
                outdated = oldTime > newTime
            }
            if (!outdated) {
                freshMessages.add(message)
            }
        }

        // update all the fresh messages
        for (message in freshMessages) {
            copy[message.id] = message
        }
        // second pass for threads
        for (message in freshMessages) {
            // prevent issues with missing cids
            message.cid = cid
            // handle threads
            val parentId = message.parentId ?: ""
            if (message.replyCount != 0) {
                // initialize the livedata object if it doesn't exist yet
                var threadMessages = mutableMapOf<String, Message>()
                if (!_threads.containsKey(message.id)) {
                    _threads[message.id] = MutableLiveData(threadMessages)
                }
                // get a copy of the data, update it and post update
                threadMessages = _threads[message.id]!!.value!!
                threadMessages[message.id] = message
                _threads[message.id]!!.postValue(threadMessages)
            } else if (parentId.isNotEmpty()) {
                // initialize the livedata object if it doesn't exist yet

                if (!_threads.containsKey(parentId)) {
                    var threadMessages = mutableMapOf<String, Message>()
                    val parent = copy[parentId]
                    parent?.let { threadMessages[it.id] = it }
                    _threads[parentId] = MutableLiveData(threadMessages)
                }
                // get a copy of the data, update it and post update
                var threadMessages = _threads[parentId]!!.value!!
                threadMessages[message.id] = message
                _threads[parentId]!!.postValue(threadMessages)
            }
        }

        _messages.postValue(copy)
    }

    override fun clean() {
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
        copy.remove(domainImpl.currentUser.id)
        _typing.postValue(copy)
    }

    private fun setHidden(hidden: Boolean) {
        if (_hidden.value != hidden) {
            _hidden.postValue(hidden)
        }
    }

    fun handleEvents(events: List<ChatEvent>) {
        // livedata actually batches many frequent updates after each other
        // we might not need a more optimized handleEvents implementation.. TBD.
        for (event in events) {
            handleEvent(event)
        }
    }

    fun isHidden(): Boolean {
        return _hidden.value ?: false
    }

    fun handleEvent(event: ChatEvent) {
        event.channel?.watcherCount?.let {
            setWatcherCount(it)
        }
        when (event) {
            is NewMessageEvent, is MessageUpdatedEvent, is MessageDeletedEvent, is NotificationMessageNew -> {
                upsertEventMessage(event.message)
                // unhide the channel
                if (isHidden()) {
                    setHidden(false)
                }
            }
            is ReactionNewEvent, is ReactionDeletedEvent -> {
                upsertEventMessage(event.message)
            }
            is MemberRemovedEvent -> {
                deleteMember(event.user!!.id)
            }
            is MemberAddedEvent, is MemberUpdatedEvent, is NotificationAddedToChannelEvent -> {
                // add /remove the members etc
                upsertMember(event.member!!)
            }

            is UserPresenceChanged -> {
                upsertUserPresence(event.user!!)
            }

            is UserUpdated -> {
                upsertUser(event.user!!)
            }

            is UserStartWatchingEvent -> {
                upsertWatcher(event.user!!)
            }
            is UserStopWatchingEvent -> {
                deleteWatcher(event.user!!)
            }
            is ChannelUpdatedEvent -> {
                event.channel?.let { updateChannelData(it) }
            }
            is ChannelHiddenEvent -> {
                event.channel?.let { setHidden(true) }
            }
            is ChannelVisible -> {
                event.channel?.let { setHidden(false) }
            }
            is TypingStopEvent -> {
                setTyping(event.user?.id!!, null)
            }
            is TypingStartEvent -> {
                setTyping(event.user?.id!!, event)
            }
            is MessageReadEvent, is NotificationMarkReadEvent -> {
                val read = ChannelUserRead(event.user!!, event.createdAt!!)
                updateRead(read)
            }
        }
    }

    private fun upsertUserPresence(user: User) {
        val userId = user.id
        // members and watchers have users
        val members = _members.value ?: mutableMapOf()
        val watchers = _watchers.value ?: mutableMapOf()
        val member = members[userId]
        val watcher = watchers[userId]
        if (member != null) {
            member.user = user
            upsertMember(member)
        }
        if (watcher != null) {
            upsertWatcher(user)
        }
    }

    private fun upsertUser(user: User) {
        upsertUserPresence(user)
        // channels have users
        val userId = user.id
        val channelData = _channelData.value
        if (channelData != null) {
            if (channelData.createdBy.id == userId) {
                channelData.createdBy = user
            }
        }

        // updating messages is harder
        // user updates don't happen frequently, it's probably ok for this update to be sluggish
        // if it turns out to be slow we can do a simple reverse index from user -> message
        val messages = _messages.value ?: mutableMapOf()
        val changedMessages = mutableListOf<Message>()
        for (message in messages.values) {
            var changed = false
            if (message.user.id == userId) {
                message.user = user
                changed = true
            }
            for (reaction in message.ownReactions) {
                if (reaction.user!!.id == userId) {
                    reaction.user = user
                    changed = true
                }
            }
            for (reaction in message.latestReactions) {
                if (reaction.user!!.id == userId) {
                    reaction.user = user
                    changed = true
                }
            }
            if (changed) changedMessages.add(message)
        }
        if (changedMessages.isNotEmpty()) {
            upsertMessages(changedMessages)
        }
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

    private fun deleteMember(userId: String) {
        val copy = _members.value ?: mutableMapOf()
        copy.remove(userId)
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
        val currentUserId = domainImpl.currentUser.id
        val copy = _reads.value ?: mutableMapOf()
        for (r in reads) {
            copy[r.getUserId()] = r
            // update read state for the current user
            if (r.getUserId() == currentUserId) {
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

    fun updateLiveDataFromChannelEntityPair(c: ChannelEntityPair) {
        setHidden(c.entity.hidden)
        hideMessagesBefore = c.entity.hideMessagesBefore
        updateLiveDataFromChannel(c.channel)
    }

    fun updateLiveDataFromChannel(c: Channel) {
        // Update all the livedata objects based on the channel
        updateChannelData(c)
        setWatcherCount(c.watcherCount)
        updateReads(c.read)

        // there are some edge cases here, this code adds to the members, watchers and messages
        // this means that if the offline sync went out of sync things go wrong
        setMembers(c.members)
        setWatchers(c.watchers)
        upsertMessages(c.messages)
    }

    private fun setMembers(members: List<Member>) {
        val copy = _members.value ?: mutableMapOf()
        for (m in members) {
            copy[m.getUserId()] = m
        }
        _members.postValue(copy)
    }

    fun updateChannelData(channel: Channel) {
        _channelData.postValue(ChannelData(channel))
    }

    fun setWatchers(watchers: List<Watcher>) {
        val copy = _watchers.value ?: mutableMapOf()
        for (watcher in watchers) {
            watcher.user.let {
                copy[it.id] = it
            }
        }

        _watchers.postValue(copy)
    }

    suspend fun editMessage(message: Message): Result<Message> {
        val online = domainImpl.isOnline()
        message.updatedAt = Date()
        message.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            message.syncStatus = SyncStatus.SYNC_NEEDED
        }

        // Update livedata
        upsertMessage(message)

        // Update Room State
        domainImpl.repos.messages.insertMessage(message)

        if (online) {
            val runnable = {
                client.updateMessage(message)
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                message.syncStatus = SyncStatus.COMPLETED
                upsertMessage(message)
                domainImpl.repos.messages.insertMessage(message)

                return Result(result.data() as Message, null)
            } else {
                if (result.error().isPermanent()) {
                    message.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    message.syncStatus = SyncStatus.SYNC_NEEDED
                }

                upsertMessage(message)
                domainImpl.repos.messages.insertMessage(message)
                return Result(null, result.error())
            }
        }
        return Result(message, null)
    }

    suspend fun deleteMessage(message: Message): Result<Message> {
        val online = domainImpl.isOnline()
        message.deletedAt = Date()
        message.syncStatus = SyncStatus.IN_PROGRESS
        if (!online) {
            message.syncStatus = SyncStatus.SYNC_NEEDED
        }

        // Update livedata
        upsertMessage(message)

        // Update Room State
        domainImpl.repos.messages.insertMessage(message)

        if (online) {
            val runnable = {
                client.deleteMessage(message.id)
            }
            val result = domainImpl.runAndRetry(runnable)
            if (result.isSuccess) {
                message.syncStatus = SyncStatus.COMPLETED
                upsertMessage(message)
                domainImpl.repos.messages.insertMessage(message)
                return Result(result.data() as Message, null)
            } else {
                if (result.error().isPermanent()) {
                    message.syncStatus = SyncStatus.FAILED_PERMANENTLY
                } else {
                    message.syncStatus = SyncStatus.SYNC_NEEDED
                }

                upsertMessage(message)
                domainImpl.repos.messages.insertMessage(message)
                return Result(null, result.error())
            }
        }
        return Result(message, null)
    }

    override fun toChannel(): Channel {
        // recreate a channel object from the various observables.
        val channelData = _channelData.value ?: ChannelData(channelType, channelId)

        val messages = sortedMessages()
        val members = (_members.value ?: mutableMapOf()).values.toList()
        val watchers = (_watchers.value ?: mutableMapOf()).values.toList()
        val reads = (_reads.value ?: mutableMapOf()).values.toList()
        val watcherCount = _watcherCount.value ?: 0

        val channel = channelData.toChannel(messages, members, reads, watchers, watcherCount)
        channel.config = getConfig()
        channel.unreadCount = computeUnreadCount(domainImpl.currentUser, _read.value, messages)
        if (messages.isNotEmpty()) {
            channel.lastMessageAt = messages.last().createdAt
        }

        return channel
    }
}
