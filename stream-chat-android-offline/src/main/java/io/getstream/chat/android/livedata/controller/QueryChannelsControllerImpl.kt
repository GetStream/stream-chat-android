package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.model.ChannelConfig
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.livedata.request.toQueryChannelsRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

internal class QueryChannelsControllerImpl(
    override val filter: FilterObject,
    override val sort: QuerySort<Channel>,
    private val client: ChatClient,
    private val domainImpl: ChatDomainImpl,
) : QueryChannelsController {
    override var newChannelEventFilter: (Channel, FilterObject) -> Boolean = { _, _ -> true }
    override var recoveryNeeded: Boolean = false

    val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter, sort)

    private val _endOfChannels = MutableStateFlow(false)
    override val endOfChannels: LiveData<Boolean> = _endOfChannels.asLiveData()

    private val _channels = MutableStateFlow<Map<String, Channel>>(emptyMap())

    private val _sortedChannels = _channels.filterNotNull()
        .map { it.values.sortedWith(sort.comparator) }.stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())

    // Keep the channel list locally sorted
    override var channels: LiveData<List<Channel>> = _sortedChannels.asLiveData()

    private val logger = ChatLogger.get("ChatDomain QueryChannelsController")

    private val _loading = MutableStateFlow(false)
    override val loading: LiveData<Boolean> = _loading.asLiveData()

    private val _loadingMore = MutableStateFlow(false)
    override val loadingMore: LiveData<Boolean> = _loadingMore.asLiveData()

    private val _channelsState: StateFlow<QueryChannelsController.ChannelsState> =
        _loading.combine(_sortedChannels) { loading: Boolean, channels: List<Channel> ->
            when {
                loading -> QueryChannelsController.ChannelsState.Loading
                channels.isEmpty() -> QueryChannelsController.ChannelsState.OfflineNoResults
                else -> QueryChannelsController.ChannelsState.Result(channels)
            }
        }.stateIn(domainImpl.scope, SharingStarted.Eagerly, QueryChannelsController.ChannelsState.NoQueryActive)

    override val channelsState = _channelsState.asLiveData()

    fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest {
        return QueryChannelsPaginationRequest(
            sort,
            _channels.value.size,
            channelLimit,
            messageLimit,
            memberLimit
        )
    }

    /**
     * Members of a channel receive the
     *
     * @see NotificationAddedToChannelEvent
     *
     * We allow you to specify a newChannelEventFilter callback to determine if this query matches the given channel
     */
    internal suspend fun addChannelIfFilterMatches(
        channel: Channel,
    ) {
        if (newChannelEventFilter(channel, filter)) {
            val channelControllerImpl = domainImpl.channel(channel)
            channelControllerImpl.updateLiveDataFromChannel(channel)
            addToQueryResult(listOf(channel.cid))
        }
    }

    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    internal suspend fun handleEvent(event: ChatEvent) {
        if (event is NotificationAddedToChannelEvent) {
            // this is the only event that adds channels to the query
            addChannelIfFilterMatches(event.channel)
        } else if (event is NotificationMessageNewEvent) {
            // It is necessary to add the channel only if it is not already present
            val channel = event.channel

            if (!queryChannelsSpec.cids.contains(channel.cid) && newChannelEventFilter(channel, filter)) {
                val channelControllerImpl = domainImpl.channel(channel)
                channelControllerImpl.updateLiveDataFromChannel(channel)
            }
        }

        if (event is MarkAllReadEvent) {
            refreshAllChannels()
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
            // The reason is that we are on the IO thread and update ChannelController using postValue()
            //  ChannelController.toChannel() can read the old version of the data using livedata.value
            // Solutions:
            // - suspend/wait for a few seconds (yuck, lets not do that)
            // - post the refresh on a livedata object with only channel ids, and transform that into channels (this ensures it will get called after postValue completes)
            // - run the refresh channel call below on the UI thread instead of IO thread
            domainImpl.scope.launch {
                refreshChannel(event.cid)
            }
        }
    }

    suspend fun loadMore(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
    ): Result<List<Channel>> {
        val pagination = loadMoreRequest(channelLimit, messageLimit)
        return runQuery(pagination)
    }

    suspend fun query(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): Result<List<Channel>> {
        return runQuery(
            QueryChannelsPaginationRequest(
                sort,
                INITIAL_CHANNEL_OFFSET,
                channelLimit,
                messageLimit,
                memberLimit
            )
        )
    }

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        val query = domainImpl.repos.selectQueryChannels(queryChannelsSpec)
            ?: return null

        return domainImpl.selectAndEnrichChannels(query.cids.toList(), pagination).also {
            logger.logI("found ${it.size} channels in offline storage")
        }
    }

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(filter, domainImpl.userPresence)
        // next run the actual query
        val response = client.queryChannels(request).execute()

        if (response.isSuccess) {
            recoveryNeeded = false

            // store the results in the database
            val channelsResponse = response.data().toSet()
            if (channelsResponse.size < pagination.channelLimit) {
                _endOfChannels.value = true
            }
            // first things first, store the configs
            val channelConfigs = channelsResponse.map { ChannelConfig(it.type, it.config) }
            domainImpl.repos.insertChannelConfigs(channelConfigs)
            logger.logI("api call returned ${channelsResponse.size} channels")
            updateQueryChannelsSpec(channelsResponse, pagination.isFirstPage)
            domainImpl.repos.insertQueryChannels(queryChannelsSpec)
            domainImpl.storeStateForChannels(channelsResponse)
        } else {
            logger.logI("Query with filter $filter failed, marking it as recovery needed")
            recoveryNeeded = true
            domainImpl.addError(response.error())
        }
        return response
    }

    private fun updateQueryChannelsSpec(channels: Collection<Channel>, isFirstPage: Boolean) {
        val newCids = channels.map(Channel::cid)
        queryChannelsSpec.cids =
            if (isFirstPage) newCids else (queryChannelsSpec.cids + newCids).distinct()
    }

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val loading = if (pagination.isFirstPage) {
            _loading
        } else {
            _loadingMore
        }

        if (loading.value) {
            logger.logI("Another query channels request is in progress. Ignoring this request.")
            return Result(
                ChatError("Another query channels request is in progress. Ignoring this request.")
            )
        }

        loading.value = true

        // start by getting the query results from offline storage
        val queryOfflineJob = domainImpl.scope.async { runQueryOffline(pagination) }

        // start the query online job before waiting for the query offline job
        val queryOnlineJob = domainImpl.scope.async { runQueryOnline(pagination) }

        val channels = queryOfflineJob.await()?.also { offlineChannels ->
            updateChannelsAndQueryResults(offlineChannels, pagination.isFirstPage)
            loading.value = offlineChannels.isEmpty()
        }

        val output: Result<List<Channel>> = queryOnlineJob.await().let { onlineResult ->
            if (onlineResult.isSuccess) {
                onlineResult.also { updateChannelsAndQueryResults(it.data(), pagination.isFirstPage) }
            } else {
                channels?.let { Result(it) } ?: onlineResult
            }
        }

        loading.value = false
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
    internal suspend fun updateChannelsAndQueryResults(
        channels: List<Channel>?,
        isFirstPage: Boolean,
    ) {
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

    internal suspend fun removeChannel(cid: String) {
        // Remove from queryChannelsSpec
        if (queryChannelsSpec.cids.contains(cid)) {
            queryChannelsSpec.cids = queryChannelsSpec.cids - cid
            domainImpl.repos.insertQueryChannels(queryChannelsSpec)
            // Remove from channel repository
            domainImpl.repos.deleteChannel(cid)

            _channels.value = _channels.value.minus(cid)
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
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    private fun refreshAllChannels() {
        refreshChannels(queryChannelsSpec.cids)
    }

    /**
     * Refreshes multiple channels on this query
     * Note that it retrieves the data from the current channelController object
     *
     * @param cIds the channels to refresh
     * @see ChannelController
     */
    private fun refreshChannels(cIds: List<String>) {
        val cIdsInQuery = queryChannelsSpec.cids.intersect(cIds)

        // update the channels
        val newChannels = cIdsInQuery.map { domainImpl.channel(it).toChannel() }
        val existingChannelMap = _channels.value.toMutableMap()

        newChannels.forEach { channel ->
            existingChannelMap[channel.cid] = channel
        }
        _channels.value = existingChannelMap
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
        queryChannelsSpec.cids = (queryChannelsSpec.cids + cIds).distinct()
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
        queryChannelsSpec.cids = cIds
        refreshChannels(cIds)
    }
}
