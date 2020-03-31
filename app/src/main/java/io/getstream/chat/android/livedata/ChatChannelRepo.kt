package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.MessageEntity
import io.getstream.chat.android.livedata.entity.ReactionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*


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
class ChatChannelRepo(var channelType: String, var channelId: String, var client: ChatClient, var repo: io.getstream.chat.android.livedata.StreamChatRepository) {

    val channelController = client.channel(channelType, channelId)
    val cid = "%s:%s".format(channelType, channelId)

    private val logger = ChatLogger.get("ChatChannelRepo")

    private val _messages = MutableLiveData<MutableMap<String, Message>>()
    /** LiveData object with the messages */

    // TODO, we could make this more efficient by using a data structure that keeps the sort
    val messages : LiveData<List<Message>> = Transformations.map(_messages) {
        it.values.sortedBy { it.createdAt }
    }

    // TODO: should we expose a loading and loading more object?

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
        it.values.sortedBy { it.name + it.id }
    }

    private val _members = MutableLiveData<MutableMap<String, Member>>()
    val members : LiveData<List<Member>> = Transformations.map(_members) {
        it.values.sortedBy { it.user.name + it.user.id }
    }

    // TODO: this is not great, individual livedata objects would be better
    val threads = Transformations.map(_messages) {
        val sorted = it.values.sortedBy { it.createdAt }
        val threads = mutableMapOf<String, MutableList<Message>>()
        for (message in sorted) {
            var parentId: String = message.parentId ?: ""
            if (message.parentId.isNullOrBlank()) {
                parentId = message.id
            }
            if (threads[parentId] == null) {
                threads[parentId] = mutableListOf()
            }
            threads[parentId]?.add(message)
        }
        threads
    }



    fun watch() {
        // TODO: messages need to update whenever the messages in room change. the transform is kinda tricky
        // because of the user enrichment though...

        GlobalScope.launch(Dispatchers.IO) {
            // first we load the data from room and update the messages and channel livedata
            val channel = repo.selectAndEnrichChannel(cid, 100)

            channel?.let {
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
            // TODO: figure out why repo.isOnline returns the wrong value
            val response = channelController.watch(ChannelWatchRequest()).execute()

            if (!response.isSuccess) {
                repo.addError(response.error())
            } else {
                val channelResponse = response.data()
                upsertMessages(channelResponse.messages)
                channelResponse.messages = emptyList()
                _channel.postValue(channelResponse)

                repo.storeStateForChannel(channelResponse)
            }

        }


    }



    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */
    fun sendMessage(message: Message) {
        message.id = repo.generateMessageId()
        message.cid = cid
        message.createdAt = message.createdAt ?: Date()
        message.syncStatus = SyncStatus.SYNC_NEEDED

        GlobalScope.launch {
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
                channelController.sendMessage(message)
            }
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
    fun sendReaction(reaction: Reaction) {
        GlobalScope.launch {
            // insert the message into local storage
            val reactionEntity = ReactionEntity(reaction)
            reactionEntity.syncStatus = SyncStatus.SYNC_NEEDED
            repo.insertReactionEntity(reactionEntity)
            // update the message in the local storage
            val messageEntity = repo.selectMessageEntity(reaction.messageId)
            messageEntity?.let {
                it.addReaction(reaction)
                repo.insertMessageEntity(it)
            }

        }


        if (repo.isOnline()) {
            client.sendReaction(reaction)
        }

    }

    fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.value = watcherCount
        }
    }

    fun upsertMessage(message: Message) {
        upsertMessages(listOf<Message>(message))
    }

    fun upsertMessages(messages: List<Message>) {
        val copy = _messages.value ?: mutableMapOf()
        for (message in messages) {
            copy.put(message.id, message)
        }
        _messages.value = copy
    }

    fun clean() {
        // Cleanup typing events that are older than 15 seconds
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
        // TODO: the current event system leads to a lot of !! in the codebase
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
                // TODO: what are members sorted on?
                upsertMember(event.member!!)
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
                updateRead(event.user!!, event.createdAt!!)
            }
        }
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

    private fun updateRead(
        user1: User,
        createdAt: Date
    ) {
        val copy = _reads.value ?: mutableMapOf()
        copy[user1.id] = ChannelUserRead().apply {user = user1; lastRead = createdAt}
        _reads.value = copy
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