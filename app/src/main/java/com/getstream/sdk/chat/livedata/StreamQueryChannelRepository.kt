package com.getstream.sdk.chat.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.getstream.sdk.chat.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.Dispatchers


/**
 * The StreamQueryChannelRepository is a small helper to show a list of channels
 *
 * - queryChannelRepo.channels a livedata object with the list of channels. this list
 * updates whenever a new message is added to one of the channels, the read state changes for members of these channels,
 * messages are edited or updated, or the channel is updated.
 */
class StreamQueryChannelRepository(var query: QueryChannelsEntity, var client: ChatClient, var repo: StreamChatRepository) {
    /**
     * A livedata object with the channels matching this query.
     */
    lateinit var channels: LiveData<List<Channel>>
    private val logger = ChatLogger.get("ChatQueryRepo")

    // TODO: handleMessageNotification should be configurable
    fun handleMessageNotification(event: NotificationAddedToChannelEvent) {
        event.channel?.let {
            query.channelCIDs.add(0, it.cid)
        }
    }

    /**
     * Run the given queryChannels request and update the channels livedata object
     */
    fun query(request: QueryChannelsRequest) {
        channels = liveData(Dispatchers.IO) {
            // start by getting the query results from offline storage
            val query = repo.selectQuery(query.id)
            // TODO: we should use a transform so it's based on the livedata perhaps?
            if (query != null) {
                val channels = repo.selectAndEnrichChannels(query.channelCIDs)


                emit(channels.toList())
            }
            // next run the actual query
            client.queryChannels(request).enqueue {
                // check for an error
                if (!it.isSuccess) {
                    repo.addError(it.error())
                }
                // store the results in the database
                val channelsResponse = it.data()

                repo.storeStateForChannels(channelsResponse)

                //TODO: either emit or rely on livedata at the storage level to make this work


            }

        }
    }
}