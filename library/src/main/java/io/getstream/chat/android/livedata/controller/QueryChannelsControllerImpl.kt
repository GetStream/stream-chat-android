package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.Call2
import io.getstream.chat.android.livedata.CallImpl2
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.*

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

class QueryChannelsControllerImpl(
    override var queryEntity: QueryChannelsEntity,
    internal var client: ChatClient,
    internal var domainImpl: ChatDomainImpl
) : QueryChannelsController {
    override var recoveryNeeded: Boolean = false
    /**
     * A livedata object with the channels matching this query.
     */

    val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + domainImpl.job + job)

    private val _endOfChannels = MutableLiveData<Boolean>(false)
    override val endOfChannels: LiveData<Boolean> = _endOfChannels

    private val _channels = MutableLiveData<ConcurrentHashMap<String, Channel>>()
    // Ensure we don't lose the sort in the channel
    override var channels: LiveData<List<Channel>> = Transformations.map(_channels) { cMap -> queryEntity.channelCIDs.mapNotNull { cMap[it] } }

    private val logger = ChatLogger.get("ChatDomain QueryChannelsController")

    private val _loading = MutableLiveData<Boolean>(false)
    override val loading: LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData<Boolean>(false)
    override val loadingMore: LiveData<Boolean> = _loadingMore

    fun loadMoreRequest(limit: Int = 30, messageLimit: Int = 10): QueryChannelsPaginationRequest {
        val channels = _channels.value ?: ConcurrentHashMap()

        return QueryChannelsPaginationRequest(channels.size, limit, messageLimit)
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

            val channel = domainImpl.channel(event.cid!!).toChannel()
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
    fun query(limit: Int = 30, messageLimit: Int = 10): Call2<List<Channel>> {
        val runnable = suspend {
            _query(limit, messageLimit)
        }
        return CallImpl2<List<Channel>>(runnable)
    }

    fun loadMore(limit: Int = 30, messageLimit: Int = 10): Call2<List<Channel>> {
        val runnable = suspend {
            _loadMore(limit, messageLimit)
        }
        return CallImpl2<List<Channel>>(runnable)
    }

    suspend fun _loadMore(limit: Int = 30, messageLimit: Int = 10): Result<List<Channel>> {
        val pagination = loadMoreRequest(limit, messageLimit)
        return runQuery(pagination)
    }

    suspend fun _query(limit: Int = 30, messageLimit: Int = 10): Result<List<Channel>> {
        return runQuery(QueryChannelsPaginationRequest(0, limit, messageLimit))
    }

    fun paginateChannelIds(channelIds: SortedSet<String>, pagination: QueryChannelsPaginationRequest): List<String> {
        var min = pagination.channelOffset
        var max = pagination.channelOffset + pagination.channelLimit
        if (max > channelIds.size - 1) {
            max = channelIds.size - 1
        }

        return if (min > channelIds.size - 1) {
            listOf()
        } else {
            channelIds.toList().slice(IntRange(min, max))
        }
    }

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        var queryEntity = domainImpl.repos.queryChannels.select(queryEntity.id)
        var channels: List<Channel>? = null

        if (queryEntity != null) {

            var channelIds = paginateChannelIds(queryEntity.channelCIDs, pagination)

            channels = domainImpl.selectAndEnrichChannels(channelIds, pagination)
            for (c in channels) {
                val channelRepo = domainImpl.channel(c)
                channelRepo.updateLiveDataFromChannel(c)
            }
            logger.logI("found ${channels.size} channels in offline storage")
        }

        if (channels != null) {
            for (c in channels) {
                val channelRepo = domainImpl.channel(c)
                channelRepo.updateLiveDataFromChannel(c)
            }
            // first page replaces the results, second page adds to them
            if (pagination.isFirstPage()) {
                setChannels(channels)
            } else {
                addChannels(channels)
            }
        }
        return channels
    }

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(queryEntity.filter, queryEntity.sort, domainImpl.userPresence)
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
            domainImpl.repos.configs.insert(configEntities)
            logger.logI("api call returned ${channelsResponse.size} channels")

            // initialize channel repos for all of these channels
            for (c in channelsResponse) {
                val channelRepo = domainImpl.channel(c)
                channelRepo.updateLiveDataFromChannel(c)
            }

            domainImpl.storeStateForChannels(channelsResponse)

            if (pagination.isFirstPage()) {
                setChannels(channelsResponse)
            } else {
                addChannels(channelsResponse)
            }
            domainImpl.repos.queryChannels.insert(queryEntity)
        } else {
            recoveryNeeded = true
            domainImpl.addError(response.error())
        }
        return response
    }

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val loader = if (pagination.isFirstPage()) {
            _loading
        } else {
            _loadingMore
        }
        if (loader.value == true) {
            logger.logI("Another query channels request is in progress. Ignoring this request.")
            return Result(null, ChatError("Another query channels request is in progress. Ignoring this request."))
        }
        loader.postValue(true)
        // start by getting the query results from offline storage

        val channels = runQueryOffline(pagination)

        // we could either wait till we are online
        // or mark ourselves as needing recovery and trigger recovery
        val output: Result<List<Channel>>
        if (domainImpl.isOnline()) {
            val result = runQueryOnline(pagination)
            output = if (result.isSuccess) {
                Result(result.data() as List<Channel>, null)
            } else {
                Result(null, result.error())
            }
        } else {
            recoveryNeeded = true
            output = Result(channels, null)
        }
        loader.postValue(false)
        return output
    }

    private fun addChannels(channelsResponse: List<Channel>, onTop: Boolean = false) {
        // second page adds to the list of channels
        if (onTop) {
            val channelIds = channelsResponse.map { it.cid }.toSortedSet()
            channelIds.addAll(queryEntity.channelCIDs)
            queryEntity.channelCIDs = channelIds
        } else {
            queryEntity.channelCIDs.addAll(channelsResponse.map { it.cid }.toMutableList())
        }
        val copy = _channels.value ?: ConcurrentHashMap()

        val missingChannels = channelsResponse.filterNot { it.cid in copy }.map { domainImpl.channel(it.cid).toChannel() }
        for (channel in missingChannels) {
            copy[channel.cid] = channel
        }
        _channels.postValue(copy)
    }

    private fun setChannels(channelsResponse: List<Channel>) {
        // first page sets the channels/overwrites..
        queryEntity.channelCIDs = channelsResponse.map { it.cid }.toSortedSet()
        val channels = channelsResponse.map { domainImpl.channel(it.cid).toChannel() }
        val channelMap = channels.associateBy { it.cid }.toMutableMap()
        val safeMap = ConcurrentHashMap(channelMap)
        _channels.postValue(safeMap)
    }
}
