package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.*
import java.util.Arrays.copyOf


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
class ChannelRepo(var channelType: String, var channelId: String, var client: ChatClient, var repo: io.getstream.chat.android.livedata.ChatRepo) {

    val channelController = client.channel(channelType, channelId)
    val cid = "%s:%s".format(channelType, channelId)

    private val logger = ChatLogger.get("ChatChannelRepo")

    private val _messages = MutableLiveData<MutableMap<String, Message>>()
    /** LiveData object with the messages */

    // TODO, we could make this more efficient by using a data structure that keeps the sort
    val messages : LiveData<List<Message>> = Transformations.map(_messages) {
        it.values.sortedBy { it.createdAt }
    }

    // TODO: what about channel data... I think it doesn't matter (handled by createChannel, but not 100% sure)

    // TODO: support user references and updating user references

    private val _channel = MutableLiveData<Channel>()
    /** LiveData object with the channel information (members, data etc.) */
    val channel : LiveData<Channel> = _channel

    private val _watcherCount = MutableLiveData<Int>()
    val watcherCount : LiveData<Int> = _watcherCount

    private val _typing = MutableLiveData<MutableMap<String, ChatEvent>>()
    val typing : LiveData<List<User>> = Transformations.map(_typing) {
        it.values.sortedBy { it.receivedAt }.map { it.user!! }
    }

    private val _reads = MutableLiveData<MutableMap<String, ChannelUserRead>>()
    val reads : LiveData<List<ChannelUserRead>> = Transformations.map(_reads) {
        it.values.sortedBy { it.lastRead }
    }

    private val _watchers = MutableLiveData<MutableMap<String, User>>()
    val watchers : LiveData<List<User>> = Transformations.map(_watchers) {
        it.values.sortedBy { it.createdAt }
    }

    private val _members = MutableLiveData<MutableMap<String, Member>>()
    val members : LiveData<List<Member>> = Transformations.map(_members) {
        it.values.sortedBy { it.createdAt }
    }

    private val _loading = MutableLiveData<Boolean>(false)
    val loading : LiveData<Boolean> = _loading

    private val _loadingOlderMessages = MutableLiveData<Boolean>(false)
    val loadingOlderMessages : LiveData<Boolean> = _loadingOlderMessages

    private val _loadingNewerMessages = MutableLiveData<Boolean>(false)
    val loadingNewerMessages : LiveData<Boolean> = _loadingNewerMessages


    val _threads : MutableMap<String, MutableLiveData<MutableMap<String, Message>>> = mutableMapOf()

    fun getThread(threadId: String): LiveData<List<Message>> {
        val threadMessageMap = _threads.getOrDefault(threadId, MutableLiveData(mutableMapOf()))
        return Transformations.map(threadMessageMap) { it.values.sortedBy { m -> m.createdAt }}
    }

    fun loadOlderMessages(limit: Int = 30) {
        GlobalScope.launch(Dispatchers.IO) {
            val request = loadMoreMessagesRequest(limit, Pagination.GREATER_THAN)
            runChannelQuery(request)
        }
    }



    fun loadNewerMessages(limit: Int = 30) {
        GlobalScope.launch(Dispatchers.IO) {
            val request = loadMoreMessagesRequest(limit, Pagination.LESS_THAN)
            runChannelQuery(request)
        }
    }

    fun sortedMessages(): List<Message> {
        // sorted ascending order, so the oldest messages are at the beginning of the list
        val messageMap = _messages.value ?: mutableMapOf()
        return messageMap.values.sortedBy { it.createdAt }
    }

    fun loadMoreMessagesRequest(limit: Int = 30, direction: Pagination): ChannelWatchRequest {
        val messages = sortedMessages()
        var request = ChannelWatchRequest().withMessages(limit)
        if (messages.isNotEmpty()) {
            val messageId: String = when(direction) {
                Pagination.GREATER_THAN_OR_EQUAL, Pagination.GREATER_THAN -> {
                    messages.last().id
                }
                Pagination.LESS_THAN, Pagination.LESS_THAN_OR_EQUAL -> {
                    messages.first().id
                }
            }
            request = ChannelWatchRequest().withMessages(direction, messageId, limit)

        }

        return request
    }

    fun watch() {
        // Support withdata
        // TODO: channelController.watch(ChannelWatchRequest().withData(data))
        GlobalScope.launch(Dispatchers.IO) {
            _watch()
        }
    }

    suspend fun _watch() {
        _loading.value = true

        // first we load the data from room and update the messages and channel livedata
        val channel = repo.selectAndEnrichChannel(cid, 100)

        channel?.let {
            _loading.value = false
            if (it.messages.isNotEmpty()) {
                upsertMessages(it.messages)
            }

        }

        // for pagination we cant use channel.messages, so discourage that
        if (channel != null) {
            channel.messages = emptyList()
            _channel.postValue(channel)
        }


        // next we run the actual API call
        if (repo.isOnline()) {
            val request = ChannelWatchRequest()
            runChannelQuery(request)

        }

    }

    suspend fun runChannelQuery(request: ChannelWatchRequest) {
        // TODO: fix direction
        _loadingNewerMessages.value = true
        val response = channelController.watch(request).execute()

        if (response.isSuccess) {
            _loading.postValue(false)
            val channelResponse = response.data()
            repo.storeStateForChannel(channelResponse)
        } else {
            _loading.postValue(false)
            repo.addError(response.error())
        }
        _loadingNewerMessages.value = false
    }

    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */
    fun sendMessage(message: Message) {
        GlobalScope.launch(Dispatchers.IO) {
            _sendMessage(message)
        }
    }

    suspend fun _sendMessage(message: Message) {
        // set defaults for id, cid and created at
        if (message.id.isEmpty()) {
            message.id = repo.generateMessageId()
        }
        if (message.cid.isEmpty()) {
            message.cid = cid
        }
        message.createdAt = message.createdAt ?: Date()
        message.syncStatus = SyncStatus.SYNC_NEEDED

        val messageEntity = MessageEntity(message)

        // Update livedata
        upsertMessage(message)
        setLastMessage(message)

        // Update Room State
        repo.insertMessage(message)

        val channelStateEntity = repo.selectChannelEntity(message.channel.cid)
        channelStateEntity?.let {
            // update channel lastMessage at and lastMessageAt
            it.addMessage(messageEntity)
            repo.insertChannelStateEntity(it)
        }

        if (repo.isOnline()) {
            val runnable = {
                channelController.sendMessage(message) as Call<Any>
            }
            repo.runAndRetry(runnable)

        }


    }

    private fun setLastMessage(message: Message) {
        val copy = _channel.value!!
        copy.lastMessageAt = message.createdAt
        _channel.value = copy
    }

    /**
     * sendReaction posts the reaction on local storage
     * message reaction count should increase, latest reactions and own_reactions should be updated
     *
     * If you're online we make the API call to sync to the server
     * If the request fails we retry according to the retry policy set on the repo
     */
    suspend fun sendReaction(reaction: Reaction) {
        // insert the message into local storage
        val reactionEntity = ReactionEntity(reaction)
        reactionEntity.syncStatus = SyncStatus.SYNC_NEEDED
        repo.insertReactionEntity(reactionEntity)
        // update the message in the local storage
        val messageEntity = repo.selectMessageEntity(reaction.messageId)
        messageEntity?.let {
            it.addReaction(reaction, repo.currentUser.id==reaction.user!!.id)
            repo.insertMessageEntity(it)
        }
        val online = repo.isOnline()
        if (online) {
            val runnable = {
                client.sendReaction(reaction) as Call<Any>
            }
            repo.runAndRetry(runnable)
        }
    }

    fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.value = watcherCount
        }
    }

    fun upsertMessage(message: Message) {
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
                if (_threads.contains(parentId)) {
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
        // Cleanup typing events that are older than 15 seconds
        val copy = _typing.value ?: mutableMapOf()
        var changed = false
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.SECOND, -15);
        val old = calendar.time
        for ((userId, typing) in copy.toList()) {
            if (typing.receivedAt.before(old)) {
                copy.remove(userId)
                changed = true
            }
        }
        if (changed) {
            _typing.value = copy
        }
    }

    fun setTyping(userId: String, event: ChatEvent?) {
        val copy = _typing.value ?: mutableMapOf()
        if (event == null) {
            copy.remove(userId)
        } else {
            copy[userId] = event
        }
        _typing.value = copy
    }

    fun handleEvent(event: ChatEvent) {
        event.channel?.watcherCount?.let {
            setWatcherCount(it)
        }
        when (event) {
            is NewMessageEvent, is MessageUpdatedEvent, is MessageDeletedEvent -> {
                upsertMessage(event.message)
            }
            is ReactionNewEvent, is ReactionDeletedEvent -> {
                upsertMessage(event.message)
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
                val read = ChannelUserRead().apply{user=event.user!!; lastRead=event.createdAt!!}
                updateRead(read)
            }
        }
    }

    private fun upsertUser(user: User?) {
        // TODO: implement me
    }

    private fun deleteWatcher(user: User) {
        val copy = _watchers.value ?: mutableMapOf()
        copy.remove(user.id)
        _watchers.value = copy
    }

    private fun upsertWatcher(user: User) {
        val copy = _watchers.value ?: mutableMapOf()
        copy[user.id] = user
        _watchers.value = copy
    }

    private fun deleteMember(member: Member) {
        val copy = _members.value ?: mutableMapOf()
        copy.remove(member.user.id)
        _members.value = copy
    }

    fun upsertMember(member: Member) {
        val copy = _members.value ?: mutableMapOf()
        copy[member.user.id] = member
        _members.value = copy
    }

    fun updateReads(
        reads: List<ChannelUserRead>
    ) {
        val copy = _reads.value ?: mutableMapOf()
        for (r in reads) {
            copy[r.getUserId()] = r
        }
        _reads.value = copy
    }

    fun updateRead(
        read: ChannelUserRead
    ) {
        updateReads(listOf(read))
    }

    fun updateLiveDataFromChannel(c: Channel) {
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
        _members.value = copy
    }

    fun updateChannel(channel: Channel) {
        _channel.value = channel
    }

    fun setWatchers(watchers: List<Watcher>) {
        val copy = _watchers.value ?: mutableMapOf()
        for (watcher in watchers) {
            watcher.user?.let {
                copy[it.id] = it
            }
        }

        _watchers.value = copy



    }

}