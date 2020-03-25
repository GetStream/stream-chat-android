package com.getstream.sdk.chat.livedata

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import com.getstream.sdk.chat.livedata.entity.ReactionEntity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.*
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
 * - channelRepo.name (livedata object with the channel name etc.)
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

    val channel = client.channel(channelType, channelId)
    val cid = "%s:%s".format(channelType, channelId)

    private val logger = ChatLogger.get("ChatChannelRepo")

    lateinit var messages: LiveData<List<Message>>

    private val _watcherCount = MutableLiveData<Int>()
    val watcherCount : LiveData<Int> = _watcherCount

    private val _watchers = MutableLiveData<List<Watcher>>()
    val watchers : LiveData<List<Watcher>> = _watchers


    /**
     * - Generate an ID
     * - Insert the message into offline storage with sync status set to Sync Needed
     * - If we're online do the send message request
     * - If the request fails we retry according to the retry policy set on the repo
     */
    fun sendMessage(message: Message) {
        message.id = repo.generateMessageId()

        // TODO: we should probably not use global scope, but a custom scope for chat
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
                channel.sendMessage(message)
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

    fun watch() {
        messages = repo.messagesForChannel(cid)
        // TODO: do the whole read and store better here

        // store the users...
        // store the messages (and all the user references in it) getUserIds(), getUsers(), enrichUsers(user objects)
        GlobalScope.launch(Dispatchers.IO) {
            val response = channel.watch(ChannelWatchRequest()).execute()

            if (!response.isSuccess) {
                repo.addError(response.error())
            } else {
                val channelResponse = response.data()
                repo.storeStateForChannel(channelResponse)
            }
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
}