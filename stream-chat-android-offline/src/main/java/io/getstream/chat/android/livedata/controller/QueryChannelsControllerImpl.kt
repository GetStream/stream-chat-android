package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.entity.ChannelConfigEntity
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.extensions.comparator
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.request.toQueryChannelsRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

internal class QueryChannelsControllerImpl(
    override val filter: FilterObject,
    override val sort: QuerySort,
    private val client: ChatClient,
    private val domainImpl: ChatDomainImpl
) : QueryChannelsController {
    override var newChannelEventFilter: (Channel, FilterObject) -> Boolean = { _, _ -> true }
    override var recoveryNeeded: Boolean = false

    val queryEntity: QueryChannelsEntity = QueryChannelsEntity(filter, sort)
    private val job = SupervisorJob()
    val scope = CoroutineScope(Dispatchers.IO + domainImpl.job + job)

    private val _endOfChannels = MutableLiveData(false)
    override val endOfChannels: LiveData<Boolean> = _endOfChannels

    private val _channels = MutableLiveData<Map<String, Channel>>()
    // Keep the channel list locally sorted
    override var channels: LiveData<List<Channel>> = Transformations.map(_channels) { cMap -> cMap.values.sortedWith(sort.comparator) }

    private val logger = ChatLogger.get("ChatDomain QueryChannelsController")

    private val _loading = MutableLiveData(false)
    override val loading: LiveData<Boolean> = _loading

    private val _loadingMore = MutableLiveData(false)
    override val loadingMore: LiveData<Boolean> = _loadingMore

    fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT
    ): QueryChannelsPaginationRequest {
        val channels = _channels.value ?: mapOf()
        return QueryChannelsPaginationRequest(sort, channels.size, channelLimit, messageLimit, memberLimit)
    }

    /**
     * Members of a channel receive the
     *
     * @see NotificationAddedToChannelEvent
     *
     * We allow you to specify a newChannelEventFilter callback to determine if this query matches the given channel
     */
    override fun addChannelIfFilterMatches(
        channel: Channel
    ) {
        if (newChannelEventFilter(channel, filter)) {
            val channelControllerImpl = domainImpl.channel(channel)
            channelControllerImpl.updateLiveDataFromChannel(channel)
            addToQueryResult(listOf(channel.cid))
        }
    }

    fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    fun handleEvent(event: ChatEvent) {
        if (event is NotificationAddedToChannelEvent) {
            // this is the only event that adds channels to the query
            addChannelIfFilterMatches(event.channel)
        }

        if (event is CidEvent) {
            // skip events that are typically not impacting the query channels overview
            if (event is UserStartWatchingEvent || event is UserStopWatchingEvent) {
                return
            }
            // update the info for that channel from the channel repo
            logger.logI("received channel event $event")

            // refresh the channels
            // Careful, it's easy to have a race condition here.
            //
            // The reason is that we are on the IO thread and update ChannelControlelr using postValue()
            //  ChannelController.toChannel() can read the old version of the data using livedata.value
            // Solutions:
            // - suspend/wait for a few seconds (yuck, lets not do that)
            // - post the refresh on a livedata object with only channel ids, and transform that into channels (this ensures it will get called after postValue completes)
            // - run the refresh channel call below on the UI thread instead of IO thread
            scope.launch(Dispatchers.Main) {
                refreshChannel(event.cid)
            }
        }
    }

    suspend fun loadMore(channelLimit: Int = CHANNEL_LIMIT, messageLimit: Int = MESSAGE_LIMIT): Result<List<Channel>> {
        val pagination = loadMoreRequest(channelLimit, messageLimit)
        return runQuery(pagination)
    }

    suspend fun query(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT
    ): Result<List<Channel>> {
        return runQuery(QueryChannelsPaginationRequest(sort, INITIAL_CHANNEL_OFFSET, channelLimit, messageLimit, memberLimit))
    }

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        val queryEntity = domainImpl.repos.queryChannels.select(queryEntity.id)
        var channels: List<Channel>? = null

        if (queryEntity != null) {
            channels = domainImpl.selectAndEnrichChannels(queryEntity.channelCids.toList(), pagination)
            logger.logI("found ${channels.size} channels in offline storage")
        }

        updateChannelsAndQueryResults(channels, pagination.isFirstPage)
        return channels
    }

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(filter, domainImpl.userPresence)
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
            domainImpl.repos.queryChannels.insert(queryEntity)
            domainImpl.storeStateForChannels(channelsResponse)
            updateChannelsAndQueryResults(channelsResponse, pagination.isFirstPage)
        } else {
            recoveryNeeded = true
            domainImpl.addError(response.error())
        }
        return response
    }

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val loader = if (pagination.isFirstPage) {
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

        val queryOfflineJob = scope.async { runQueryOffline(pagination) }
        // start the query online job before waiting for the query offline job
        val queryOnlineJob = if (domainImpl.isOnline()) {
            scope.async { runQueryOnline(pagination) }
        } else { null }
        val channels = queryOfflineJob.await()

        // we could either wait till we are online
        // or mark ourselves as needing recovery and trigger recovery
        val output: Result<List<Channel>> = if (queryOnlineJob != null) {
            queryOnlineJob.await()
        } else {
            recoveryNeeded = true
            channels?.let { Result(it) } ?: Result(error = ChatError(message = "Channels Query wasn't run online and the offline storage is empty"))
        }
        loader.postValue(false)
        return output
    }

    /**
     * Updates the state on the channelController based on the channel object we received
     * This is used for both the online and offline query flow
     *
     * @param channels the list of channels to update
     * @param isFirstPage if it's the first page we set/replace the list of results. if it's not the first page we add to the list
     *
     */
    private fun updateChannelsAndQueryResults(channels: List<Channel>?, isFirstPage: Boolean) {
        if (channels != null) {
            val cIds = channels.map { it.cid }
            // initialize channel repos for all of these channels
            for (c in channels) {
                val channelController = domainImpl.channel(c)
                channelController.updateLiveDataFromChannel(c)
            }
            // if it's the first page, we replace the current results
            if (isFirstPage) {
                setQueryResult(cIds)
            } else {
                addToQueryResult(cIds)
            }
        }
    }

    /**
     * refreshes a single channel
     * Note that this only refreshes channels that are already matching with the query
     * It retrieves the data from the current channelController object
     *
     * @param cId the channel to update
     *
     * If you want to add to the list of channels use the addToQueryResult method
     *
     * @see addToQueryResult
     */
    fun refreshChannel(cId: String) {
        refreshChannels(listOf(cId))
    }

    /**
     * Refreshes multiple channels on this query
     * Note that it retrieves the data from the current channelController object
     *
     * @param cIds the channels to refresh
     * @see ChannelController
     */
    private fun refreshChannels(cIds: List<String>) {
        val cIdsInQuery = queryEntity.channelCids.intersect(cIds)
        val newChannels = cIdsInQuery.map { domainImpl.channel(it).toChannel() }
        val existingChannelMap = _channels.value?.toMutableMap() ?: mutableMapOf()

        newChannels.forEach { channel ->
            existingChannelMap[channel.cid] = channel
        }

        _channels.postValue(existingChannelMap)
    }

    /**
     * Adds the list of channels to the current query.
     * Channels are sorted based on the specified QuerySort
     * Triggers a refresh of these channels based on the current state on the ChannelController
     *
     * @param cIds the list of channel ids to add to the query result
     *
     * @see QuerySort
     * @see ChannelController
     */
    private fun addToQueryResult(cIds: List<String>) {
        queryEntity.channelCids = (queryEntity.channelCids + cIds).distinct()
        refreshChannels(cIds)
    }

    /**
     * Replaces the existing list of results for this query with a new list of channels
     * Channels are sorted based on the specified QuerySort
     * Triggers a refresh of these channels based on the current state on the ChannelController
     *
     * @param cIds the new list of channels
     * @see QuerySort
     * @see ChannelController
     */
    private fun setQueryResult(cIds: List<String>) {
        // If you query for page 1 we remove the old data
        queryEntity.channelCids = cIds
        refreshChannels(cIds)
    }
}
