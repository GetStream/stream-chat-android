package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toQueryChannelsRequest
import io.getstream.chat.android.offline.utils.filter
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

public class QueryChannelsController internal constructor(
    public val filter: FilterObject,
    public val sort: QuerySort<Channel>,
    private val client: ChatClient,
    private val domainImpl: ChatDomainImpl,
) {

    internal constructor(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        client: ChatClient,
        domain: ChatDomain,
    ) : this(filter, sort, client, domain as ChatDomainImpl)

    public var newChannelEventFilter: (Channel, FilterObject) -> Boolean =
        { channel, filterObject -> filterObject.filter(channel) }
    public var recoveryNeeded: Boolean = false

    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter, sort)

    private val _channels = MutableStateFlow<Map<String, Channel>>(emptyMap())
    private val _loading = MutableStateFlow(false)
    private val _loadingMore = MutableStateFlow(false)
    private val _endOfChannels = MutableStateFlow(false)
    private val _sortedChannels = _channels.map { it.values.sortedWith(sort.comparator) }
        .stateIn(domainImpl.scope, SharingStarted.Eagerly, emptyList())
    private val _mutedChannelIds = MutableStateFlow<List<String>>(emptyList())

    public val loading: StateFlow<Boolean> = _loading
    public val loadingMore: StateFlow<Boolean> = _loadingMore
    public val endOfChannels: StateFlow<Boolean> = _endOfChannels
    public val channels: StateFlow<List<Channel>> = _sortedChannels
    public val mutedChannelIds: StateFlow<List<String>> = _mutedChannelIds

    public val channelsState: StateFlow<QueryChannelsController.ChannelsState> =
        _loading.combine(_sortedChannels) { loading: Boolean, channels: List<Channel> ->
            when {
                loading -> QueryChannelsController.ChannelsState.Loading
                channels.isEmpty() -> QueryChannelsController.ChannelsState.OfflineNoResults
                else -> QueryChannelsController.ChannelsState.Result(channels)
            }
        }.stateIn(domainImpl.scope, SharingStarted.Eagerly, QueryChannelsController.ChannelsState.NoQueryActive)

    private val logger = ChatLogger.get("ChatDomain QueryChannelsController")

    internal fun loadMoreRequest(
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
     * Members of a channel receive the NotificationAddedToChannelEvent
     *
     * @see NotificationAddedToChannelEvent
     *
     * We allow you to specify a newChannelEventFilter callback to determine if this query matches the given channel
     */
    internal fun addChannelIfFilterMatches(channel: Channel) {
        if (newChannelEventFilter(channel, filter)) {
            val channelControllerImpl = domainImpl.channel(channel)
            channelControllerImpl.updateDataFromChannel(channel)
            addToQueryResult(listOf(channel.cid))
        }
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
    internal fun addToQueryResult(cIds: List<String>) {
        queryChannelsSpec.cids = (queryChannelsSpec.cids + cIds).distinct()
        refreshChannels(cIds)
    }

    internal fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    internal fun handleEvent(event: ChatEvent) {
        when (event) {
            is NotificationAddedToChannelEvent -> addChannelIfFilterMatches(event.channel)
            is ChannelUpdatedEvent -> {
                if (!queryChannelsSpec.cids.contains(event.channel.cid)) {
                    addChannelIfFilterMatches(event.channel)
                }
            }
            is ChannelUpdatedByUserEvent -> {
                if (!queryChannelsSpec.cids.contains(event.channel.cid)) {
                    addChannelIfFilterMatches(event.channel)
                }
            }
            is NotificationMessageNewEvent -> {
                if (!queryChannelsSpec.cids.contains(event.channel.cid)) {
                    addChannelIfFilterMatches(event.channel)
                }
            }
        }

        if (event is MarkAllReadEvent) {
            refreshAllChannels()
        }

        if (event is NotificationChannelMutesUpdatedEvent) {
            _mutedChannelIds.value = event.me.channelMutes.toChannelsId()
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

        if (event is UserPresenceChangedEvent) {
            refreshMembersStateForUser(event.user)
        }
    }

    /**
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    private fun refreshAllChannels() {
        refreshChannels(queryChannelsSpec.cids)
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
    public fun refreshChannel(cId: String) {
        refreshChannels(listOf(cId))
    }

    private fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id

        val affectedChannels = _channels.value
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    }
                )
            }

        _channels.value += affectedChannels
    }

    /**
     * Refreshes multiple channels on this query
     * Note that it retrieves the data from the current channelController object
     *
     * @param cIds the channels to refresh
     * @see ChannelController
     */
    internal fun refreshChannels(cIds: List<String>) {
        val cIdsInQuery = queryChannelsSpec.cids.intersect(cIds)

        // update the channels
        val newChannels = cIdsInQuery.map { domainImpl.channel(it).toChannel() }

        val existingChannelMap = _channels.value.toMutableMap()

        newChannels.forEach { channel ->
            if (newChannelEventFilter(channel, filter)) {
                existingChannelMap[channel.cid] = channel
            } else {
                domainImpl.scope.launch {
                    removeChannel(channel.cid)
                }
            }
        }

        _channels.value = existingChannelMap
    }

    internal suspend fun loadMore(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
    ): Result<List<Channel>> {
        val pagination = loadMoreRequest(channelLimit, messageLimit)
        return runQuery(pagination)
    }

    internal suspend fun removeChannel(cid: String) {
        // Remove from queryChannelsSpec
        if (queryChannelsSpec.cids.contains(cid)) {
            queryChannelsSpec.cids = queryChannelsSpec.cids - cid
            domainImpl.repos.insertQueryChannels(queryChannelsSpec)
            // Remove from channel repository
            domainImpl.repos.deleteChannel(cid)

            _channels.value = _channels.value - cid
        }
    }

    internal suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
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
                channels?.let(::Result) ?: onlineResult
            }
        }

        loading.value = false
        return output
    }

    public suspend fun query(
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
                memberLimit,
            )
        )
    }

    /**
     * Updates the state on the channelController based on the channel object we received
     * This is used for both the online and offline query flow
     *
     * @param channels the list of channels to update
     * @param isFirstPage if it's the first page we set/replace the list of results. if it's not the first page we add to the list
     *
     */
    internal fun updateChannelsAndQueryResults(
        channels: List<Channel>?,
        isFirstPage: Boolean,
    ) {
        if (channels != null) {
            val cIds = channels.map { it.cid }
            // initialize channel repos for all of these channels
            for (c in channels) {
                val channelController = domainImpl.channel(c)
                channelController.updateDataFromChannel(c)
            }
            // if it's the first page, we replace the current results
            if (isFirstPage) {
                setQueryResult(cIds)
            } else {
                addToQueryResult(cIds)
            }
        }
    }

    internal suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
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

    internal fun updateQueryChannelsSpec(channels: Collection<Channel>, isFirstPage: Boolean) {
        val newCids = channels.map(Channel::cid)
        queryChannelsSpec.cids =
            if (isFirstPage) newCids else (queryChannelsSpec.cids + newCids).distinct()
    }

    internal suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        val query = domainImpl.repos.selectById(queryChannelsSpec.id)
            ?: return null

        return domainImpl.selectAndEnrichChannels(query.cids.toList(), pagination).also {
            logger.logI("found ${it.size} channels in offline storage")
        }
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

    private fun List<ChannelMute>.toChannelsId() = map { channelMute -> channelMute.channel.id }
}
