package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.requests.QueryChannelsPaginationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


/**
 * The StreamQueryChannelRepository is a small helper to show a list of channels
 *
 * - queryChannelRepo.channels a livedata object with the list of channels. this list
 * updates whenever a new message is added to one of the channels, the read state changes for members of these channels,
 * messages are edited or updated, or the channel is updated.
 */
class QueryChannelsRepo(var query: QueryChannelsEntity, var client: ChatClient, var repo: ChatRepo) {
    /**
     * A livedata object with the channels matching this query.
     */
    private val _channels = MutableLiveData<List<Channel>>()
    var channels: LiveData<List<Channel>> = _channels

    private val logger = ChatLogger.get("QueryChannelsRepo")

    private val _loading = MutableLiveData<Boolean>(false)
    val loading : LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>(false)
    val loadingMore : LiveData<Boolean> = _loadingMore

    // TODO: test me
    fun loadMore(limit: Int = 30) {
        GlobalScope.launch(Dispatchers.IO) {
            val request = loadMoreRequest(limit)
            runQuery(request)
        }
    }

    fun loadMoreRequest(limit: Int = 30, messageLimit: Int = 10): QueryChannelsPaginationRequest {
        val channels = _channels.value ?: emptyList()
        var request = QueryChannelsPaginationRequest().withLimit(limit).withOffset(channels.size).withMessages(messageLimit)

        return request
    }


    // TODO: handleMessageNotification should be configurable
    fun handleMessageNotification(event: NotificationAddedToChannelEvent) {
        event.channel?.let {
            query.channelCIDs.add(0, it.cid)
        }
    }

    fun handleEvent(event: ChatEvent) {
        if (event is NotificationAddedToChannelEvent) {
            handleMessageNotification(event)
        }
    }

    /**
     * Run the given queryChannels request and update the channels livedata object
     */
    fun query(request: QueryChannelsPaginationRequest) {
        GlobalScope.launch(Dispatchers.IO) {
            runQuery(request)
        }
    }
    suspend fun runQuery(pagination: QueryChannelsPaginationRequest) {
        // start by getting the query results from offline storage
        val request = pagination.toQueryChannelsRequest(query.filter, query.sort, repo.userPresence)
        val query = repo.selectQuery(query.id)
        if (query != null) {
            val channels = repo.selectAndEnrichChannels(query.channelCIDs)
            _channels.postValue(channels)
        }
        val online = repo.isOnline()
        if (online) {
            // next run the actual query
            val response = client.queryChannels(request).execute()

            // check for an error
            if (!response.isSuccess) {
                repo.addError(response.error())
            } else {
                // store the results in the database
                val channelsResponse = response.data()
                _channels.postValue(channelsResponse)
                repo.storeStateForChannels(channelsResponse)
            }
        }
    }
}