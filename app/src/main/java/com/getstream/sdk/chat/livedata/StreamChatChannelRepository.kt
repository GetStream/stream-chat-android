package com.getstream.sdk.chat.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
import io.getstream.chat.android.client.utils.SyncStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


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
class StreamChatChannelRepository(var channelType: String, var channelId: String, var client: ChatClient, var repo: StreamChatRepository) {

    val channelController = client.channel(channelType, channelId)
    val cid = "%s:%s".format(channelType, channelId)

    private val logger = ChatLogger.get("ChatChannelRepo")

    private val _messages = MutableLiveData<List<Message>>()
    /** LiveData object with the messages */
    val messages : LiveData<List<Message>> = _messages

    // TODO: should we expose a loading and loading more object?

    private val _channel = MutableLiveData<Channel>()
    /** LiveData object with the channel information (members, data etc.) */
    val channel : LiveData<Channel> = _channel

    private val _watcherCount = MutableLiveData<Int>()
    val watcherCount : LiveData<Int> = _watcherCount

    private val _watchers = MutableLiveData<List<Watcher>>()
    val watchers : LiveData<List<Watcher>> = _watchers


    fun watch() {
        // TODO: messages need to update whenever the messages in room change. the transform is kinda tricky
        // because of the user enrichment though...

        GlobalScope.launch(Dispatchers.IO) {
            // first we load the data from room and update the messages and channel livedata
            val channel = repo.selectAndEnrichChannel(cid, 100)

            channel?.let {
                if (it.messages.isNotEmpty()) {
                    _messages.postValue(it.messages)
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
                _messages.postValue(channelResponse.messages)
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
        message.channel = _channel.value!!

        GlobalScope.launch {
            val messageEntity = MessageEntity(message)

            messageEntity.syncStatus = SyncStatus.SYNC_NEEDED
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



    fun setWatchers(watchers: List<Watcher>) {
        if (watchers != _watchers.value) {
            _watchers.value = watchers
        }
    }

    fun setWatcherCount(watcherCount: Int) {
        if (watcherCount != _watcherCount.value) {
            _watcherCount.value = watcherCount
        }
    }

    fun upsertMessage(message: Message) {
        // TODO: is there a cleaner way?
        val copy = _messages.value ?: mutableListOf()
        val mutableCopy = copy.toMutableList()
        mutableCopy.add(message)
        _messages.value = mutableCopy.toList()
    }
}