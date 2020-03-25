package com.getstream.sdk.chat.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.getstream.sdk.chat.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.User
import kotlinx.coroutines.Dispatchers

/**
 * The QueryChannels repository exposes the following livedata objects
 *
 * - queryChannelRepo.channels a livedata object with the list of channels. this list
 * updates whenever a new message is added to one of the channels, the read state changes for members of these channels,
 * messages are edited or updated, or the channel is updated.
 */
class StreamQueryChannelRepository(var query: QueryChannelsEntity, var client: ChatClient, var repo: StreamChatRepository) {
    lateinit var channels: LiveData<List<Channel>>
    private val logger = ChatLogger.get("ChatQueryRepo")

    fun refresh() {

    }

    fun handleMessageNotification(event: NotificationAddedToChannelEvent) {
        event.channel?.let {
            query.channelCIDs.add(0, it.cid)
        }
    }

    fun query(request: QueryChannelsRequest) {
        channels = liveData(Dispatchers.IO) {
            // start by getting the query results from offline storage
            val query = repo.selectQuery(query.id)
            // TODO: we should use a transform so it's based on the livedata perhaps?
            if (query != null) {
                val channelEntities = repo.selectChannelEntities(query.channelCIDs)


                // TODO: I should use sets for many of these
                // gather all the user ids
                val userIds = mutableListOf<String>()
                for (channelEntity in channelEntities) {
                    channelEntity.createdByUserId?.let { userIds.add(it) }
                    channelEntity.members?.let {
                        for (member in it) {
                            userIds.add(member.userId)
                        }
                    }
                    channelEntity.lastMessage?.let {
                        userIds.add(it.userId)
                    }
                }
                val userEntities = repo.selectUsers(userIds)
                val userMap = mutableMapOf<String, User>()
                for (userEntity in userEntities) {
                    userMap[userEntity.id] = userEntity.toUser()
                }

                val channels = mutableListOf<Channel>()
                for (channelEntity in channelEntities) {
                    val channel = channelEntity.toChannel(userMap)
                    channels.add(channel)
                }

                emit(channels)
            }
            // next run the actual query
            client.queryChannels(request).enqueue {
                // TODO: This storage logic can be merged with the StreamChatChannelRepo
                // TODO store the channel configs



                // check for an error
                if (!it.isSuccess) {
                    repo.addError(it.error())
                }
                // store the results in the database
                val channelsResponse = it.data()
                val users = mutableListOf<User>()
                val configs :MutableMap<String, Config> = mutableMapOf()
                for (channel in channelsResponse) {
                    users.add(channel.createdBy)
                    configs[channel.type] = channel.config
                    // TODO member loop
                }

                // store the channel configs
                repo.insertConfigs(configs)

                // store the users
                repo.insertUsers(users)
                // store the channel info
                repo.insertChannels(channelsResponse)



            }

        }
    }
}