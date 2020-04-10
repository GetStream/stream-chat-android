package io.getstream.chat.android.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.requests.QueryChannelsPaginationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

// TODO: move this to the LLC at some point
fun ChatEvent.isChannelEvent(): Boolean =  !this.cid.isNullOrEmpty() && this.cid != "*"

/**
 * The StreamQueryChannelRepository is a small helper to show a list of channels
 *
 * - queryChannelRepo.channels a livedata object with the list of channels. this list
 * updates whenever a new message is added to one of the channels, the read state changes for members of these channels,
 * messages are edited or updated, or the channel is updated.
 */
class QueryChannelsRepo(var queryEntity: QueryChannelsEntity, var client: ChatClient, var repo: ChatRepo) {
    var recoveryNeeded: Boolean = false
    /**
     * A livedata object with the channels matching this query.
     */



    private val _endOfChannels = MutableLiveData<Boolean>(false)
    val endOfChannels : LiveData<Boolean> = _endOfChannels


    private val _channels = MutableLiveData<ConcurrentHashMap<String, Channel>>()
    // Ensure we don't lose the sort in the channel
    var channels: LiveData<List<Channel>> = Transformations.map(_channels) {it.values.toList().filter{queryEntity.channelCIDs.contains(it.cid)}.sortedBy { queryEntity.channelCIDs.indexOf(it.cid) }}

    private val logger = ChatLogger.get("QueryChannelsRepo")

    private val _loading = MutableLiveData<Boolean>(false)
    val loading : LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>(false)
    val loadingMore : LiveData<Boolean> = _loadingMore

    fun loadMore(limit: Int = 30, messageLimit: Int = 10) {
        GlobalScope.launch(Dispatchers.IO) {
            val request = loadMoreRequest(limit, messageLimit)
            runQuery(request)
        }
    }

    fun loadMoreRequest(limit: Int = 30, messageLimit: Int = 10): QueryChannelsPaginationRequest {
        val channels = _channels.value ?: ConcurrentHashMap()
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
        if (event.isChannelEvent()) {
            // skip events that are typically not impacting the query channels overview
            if (event is UserStartWatchingEvent || event is UserStopWatchingEvent) {
                return
            }
            // update the info for that channel from the channel repo
            logger.logI("received channel event $event")

            val channel = repo.channel(event.cid!!).toChannel()
            updateChannel(channel)
        }
    }

    fun updateChannel(c: Channel) {
        val copy = _channels.value ?: ConcurrentHashMap()
        copy[c.id] = c
        _channels.postValue(copy)
    }

    /**
     * Run the given queryChannels request and update the channels livedata object
     */
    fun query(limit: Int = 30, messageLimit: Int = 10) {
        GlobalScope.launch(Dispatchers.IO) {
            runQuery(QueryChannelsPaginationRequest(0, limit, messageLimit))
        }
    }

    suspend fun runQueryOffline(pagination : QueryChannelsPaginationRequest): List<Channel>? {
        var queryEntity = repo.selectQuery(queryEntity.id)
        var channels : List<Channel>? = null
        if (queryEntity != null) {
            channels = repo.selectAndEnrichChannels(queryEntity.channelCIDs, pagination.messageLimit)
            for (c in channels) {
                val channelRepo = repo.channel(c)
                channelRepo.updateLiveDataFromChannel(c)
            }
            logger.logI("found ${channels.size} channels in offline storage")
        }
        return channels
    }

    suspend fun runQueryOnline(pagination : QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(queryEntity.filter, queryEntity.sort, repo.userPresence)
        // next run the actual query
        val response = client.queryChannels(request).execute()

        if (response.isSuccess) {
            recoveryNeeded = false



            // store the results in the database
            val channelsResponse = response.data()
            if (channelsResponse.size < pagination.limit) {
                _endOfChannels.postValue(true)
            }
            // first things first, store the configs
            val configEntities = channelsResponse.associateBy { it.type }.values.map {ChannelConfigEntity(it.type, it.config)}
            repo.insertConfigEntities(configEntities)
            logger.logI("api call returned ${channelsResponse.size} channels")
            // update the results stored in the db
            if (pagination.isFirstPage()) {
                val cids = channelsResponse.map{it.cid}
                queryEntity.channelCIDs = cids.toMutableList()
                repo.insertQuery(queryEntity)
            }

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
            recoveryNeeded = true
            repo.addError(response.error())
        }
        return response
    }

    suspend fun runQuery(pagination : QueryChannelsPaginationRequest) {
        val loader = if(pagination.isFirstPage()) {_loading} else {
            _loadingMore
        }
        if (loader.value == true) {
            logger.logI("Another query channels request is in progress. Ignoring this request.")
            return
        }
        loader.postValue(true)
        // start by getting the query results from offline storage

        val channels = runQueryOffline(pagination)

        if (channels != null) {
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

        // we could either wait till we are online
        // or mark ourselves as needing recovery and trigger recovery
        val online = repo.isOnline()
        if (online) {
            runQueryOnline(pagination)

        } else {
            recoveryNeeded = true
        }
        loader.postValue(false)
    }

    private fun addChannels(channelsResponse: List<Channel>) {
        val copy = _channels.value ?: ConcurrentHashMap()

        val missingChannels = channelsResponse.filterNot { it.cid in copy }.map { repo.channel(it.cid).toChannel() }
        for (channel in missingChannels) {
            copy[channel.cid] = channel
        }
        _channels.postValue(copy)
    }

    private fun setChannels(channelsResponse: List<Channel>) {
        val channels = channelsResponse.map { repo.channel(it.cid).toChannel() }
        val channelMap = channels.associateBy { it.cid }.toMutableMap()
        val safeMap = ConcurrentHashMap(channelMap)
        _channels.postValue(safeMap)
    }
}