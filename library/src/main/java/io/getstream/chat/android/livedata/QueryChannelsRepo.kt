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

    private val _channels = MutableLiveData<List<ChannelRepo>>()
    // TODO: perhaps call this channelRepos
    var channels: LiveData<List<ChannelRepo>> = _channels

    private val logger = ChatLogger.get("QueryChannelsRepo")

    private val _loading = MutableLiveData<Boolean>(false)
    val loading : LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>(false)
    val loadingMore : LiveData<Boolean> = _loadingMore

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


    // TODO 1.1: handleMessageNotification should be configurable
    fun handleMessageNotification(event: NotificationAddedToChannelEvent) {
        event.channel?.let {
            addChannels(listOf(it))
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
        val loader = if(pagination.isFirstPage()) {_loading} else {
            _loadingMore
        }
        loader.value = true
        // start by getting the query results from offline storage
        val request = pagination.toQueryChannelsRequest(query.filter, query.sort, repo.userPresence)
        val query = repo.selectQuery(query.id)
        if (query != null) {
            val channels = repo.selectAndEnrichChannels(query.channelCIDs)
            for (c in channels) {
                val channelRepo = repo.channel(c)
                channelRepo.updateLiveDataFromChannel(c)
            }
            if (pagination.isFirstPage()) {
                setChannels(channels)
            } else {
                addChannels(channels)
            }
        }
        val online = repo.isOnline()
        if (online) {
            // next run the actual query
            val response = client.queryChannels(request).execute()

            if (response.isSuccess) {
                // store the results in the database
                val channelsResponse = response.data()

                // initialize channel repos for all of these channels
                for (c in channelsResponse) {
                    val channelRepo = repo.channel(c)
                    channelRepo.updateLiveDataFromChannel(c)
                }

                repo.storeStateForChannels(channelsResponse)

                if (pagination.isFirstPage()) {
                    setChannels(channelsResponse)
                } else {
                    addChannels(channelsResponse)
                }
            } else {
                repo.addError(response.error())
            }
        }
        loader.value = false
    }

    private fun addChannels(channelsResponse: List<Channel>) {
        val copy = _channels.value?.toMutableList() ?: mutableListOf()
        val copyMap = copy.associateBy { it.cid }
        val missingRepos = channelsResponse.filterNot { it.cid in copyMap }.map { repo.channel(it.cid) }
        copy.addAll(missingRepos)
        _channels.postValue(copy)
    }

    private fun setChannels(channelsResponse: List<Channel>) {
        val repos = channelsResponse.map { repo.channel(it.cid) }
        _channels.postValue(repos)
    }
}