package com.getstream.sdk.chat.livedata

import android.util.Log
import androidx.lifecycle.LiveData
import com.getstream.sdk.chat.livedata.entity.MessageEntity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.ChannelWatchRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
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
    // TODO: this needs a transform... perhaps..
    lateinit var messages: LiveData<List<MessageEntity>>

    fun watch() {
        messages = repo.messagesForChannel(cid)

        // store the users...
        // store the messages (and all the user references in it) getUserIds(), getUsers(), enrichUsers(user objects)
        GlobalScope.launch(Dispatchers.IO) {
            // TODO: handle errors
            val response = channel.watch(ChannelWatchRequest()).execute()

            if (!response.isSuccess) {
                repo.addError(response.error())
            } else {
                val channelResponse = response.data()
                // get all the users mentioned here
                val users = mutableListOf<User>()
                users.add(channelResponse.createdBy)
                for (member in channelResponse.members) {
                    users.add(member.user)
                }
                // TODO think we should actually not store watchers (since you can have millions and it changes, should just keep this data in memory only)
                for (watcher in channelResponse.watchers) {
                    watcher.user?.let { users.add(it) }
                }
                for (message in channelResponse.messages) {
                    users.add(message.user)
                    for (reaction in message.latestReactions) {
                        reaction.user?.let { users.add(it) }
                    }
                }
                // insert users
                repo.insertUsers(users)

                // store the messages
                repo.insertMessages(channelResponse.messages)

                // store the channel
                repo.insertChannel(channelResponse)
            }




        }


    }
}