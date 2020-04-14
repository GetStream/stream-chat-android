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
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.ConcurrentHashMap

// TODO: move these to the LLC at some point
fun ChatEvent.isChannelEvent(): Boolean = !this.cid.isNullOrEmpty() && this.cid != "*"

fun Message.users(): List<User> {
    val users = mutableListOf<User>()
    users.add(this.user)
    for (reaction in this.latestReactions) {
        reaction.user?.let { users.add(it) }
    }
    return users
}

fun Channel.users(): List<User> {
    val users = mutableListOf<User>()
    users.add(this.createdBy)
    for (member in this.members) {
        users.add(member.user)
    }
    for (read in this.read) {
        users.add(read.user)
    }
    return users
}

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
    val endOfChannels: LiveData<Boolean> = _endOfChannels


    private val _channels = MutableLiveData<ConcurrentHashMap<String, Channel>>()
    // Ensure we don't lose the sort in the channel
    var channels: LiveData<List<Channel>> = Transformations.map(_channels) { it.values.toList().filter { queryEntity.channelCIDs.contains(it.cid) }.sortedBy { queryEntity.channelCIDs.indexOf(it.cid) } }

    private val logger = ChatLogger.get("QueryChannelsRepo")

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>(false)
    val loadingMore: LiveData<Boolean> = _loadingMore

    fun loadMore(limit: Int = 30, messageLimit: Int = 10) {
        GlobalScope.launch(Dispatchers.IO) {
            val pagination = loadMoreRequest(limit, messageLimit)
            runQuery(pagination)
        }
    }

    fun loadMoreRequest(limit: Int = 30, messageLimit: Int = 10): QueryChannelsPaginationRequest {
        val channels = _channels.value ?: ConcurrentHashMap()
        var request = QueryChannelsPaginationRequest(channels.size, limit, messageLimit)

        return request
    }


    // TODO 1.1: handleMessageNotification should be configurable
    fun handleMessageNotification(event: NotificationAddedToChannelEvent) {
        event.channel?.let {
            addChannels(listOf(it), true)
        }
    }

    fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
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

    fun paginateChannelIds(channelIds: SortedSet<String>, pagination: QueryChannelsPaginationRequest): List<String> {
        var min = pagination.channelOffset
        var max = pagination.channelOffset + pagination.channelLimit
        if (max > channelIds.size - 1) {
            max = channelIds.size - 1
        }

        if (min > channelIds.size - 1) {
            return listOf()
        } else {
            return channelIds.toList().slice(IntRange(min, max))
        }
    }

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        var queryEntity = repo.repos.queryChannels.selectQuery(queryEntity.id)
        var channels: List<Channel>? = null

        if (queryEntity != null) {

            var channelIds = paginateChannelIds(queryEntity.channelCIDs, pagination)

            channels = repo.selectAndEnrichChannels(channelIds, pagination)
            for (c in channels) {
                val channelRepo = repo.channel(c)
                channelRepo.updateLiveDataFromChannel(c)
            }
            logger.logI("found ${channels.size} channels in offline storage")
        }
        return channels
    }

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(queryEntity.filter, queryEntity.sort, repo.userPresence)
        // next run the actual query
        val response = client.queryChannels(request).execute()

        if (response.isSuccess) {
            recoveryNeeded = false


            // store the results in the database
            val channelsResponse = response.data()
            if (channelsResponse.size < pagination.channelLimit) {
                _endOfChannels.postValue(true)
            }
            // first things first, store the configs
            val configEntities = channelsResponse.associateBy { it.type }.values.map { ChannelConfigEntity(it.type, it.config) }
            repo.repos.configs.insert(configEntities)
            logger.logI("api call returned ${channelsResponse.size} channels")


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
            repo.repos.queryChannels.insert(queryEntity)
        } else {
            recoveryNeeded = true
            repo.addError(response.error())
        }
        return response
    }

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest) {
        val loader = if (pagination.isFirstPage()) {
            _loading
        } else {
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
            // first page replaces the results, second page adds to them
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

    private fun addChannels(channelsResponse: List<Channel>, onTop: Boolean =false) {
        // second page adds to the list of channels
        if (onTop) {
            val channelIds = channelsResponse.map { it.cid }.toSortedSet()
            channelIds.addAll(queryEntity.channelCIDs)
            queryEntity.channelCIDs = channelIds
        } else {
            queryEntity.channelCIDs.addAll(channelsResponse.map { it.cid }.toMutableList())
        }
        val copy = _channels.value ?: ConcurrentHashMap()

        val missingChannels = channelsResponse.filterNot { it.cid in copy }.map { repo.channel(it.cid).toChannel() }
        for (channel in missingChannels) {
            copy[channel.cid] = channel
        }
        _channels.postValue(copy)
    }

    private fun setChannels(channelsResponse: List<Channel>) {
        // first page sets the channels/overwrites..
        queryEntity.channelCIDs = channelsResponse.map { it.cid }.toSortedSet()
        val channels = channelsResponse.map { repo.channel(it.cid).toChannel() }
        val channelMap = channels.associateBy { it.cid }.toMutableMap()
        val safeMap = ConcurrentHashMap(channelMap)
        _channels.postValue(safeMap)
    }
}