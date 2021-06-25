package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
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
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toQueryChannelsRequest
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

    private var channelOffset = INITIAL_CHANNEL_OFFSET
    public var newChannelEventFilter: suspend (Channel, FilterObject) -> Boolean = { channel, filterObject ->
        client.queryChannels(
            QueryChannelsRequest(
                filter = Filters.and(
                    filterObject,
                    Filters.eq("cid", channel.cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
            .map { channels -> channels.any { it.cid == channel.cid } }
            .let { it.isSuccess && it.data() }
    }

    public var recoveryNeeded: Boolean = false

    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter)

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

    public val channelsState: StateFlow<ChannelsState> =
        _loading.combine(_sortedChannels) { loading: Boolean, channels: List<Channel> ->
            when {
                loading -> ChannelsState.Loading
                channels.isEmpty() -> ChannelsState.OfflineNoResults
                else -> ChannelsState.Result(channels)
            }
        }.stateIn(domainImpl.scope, SharingStarted.Eagerly, ChannelsState.NoQueryActive)

    private val logger = ChatLogger.get("ChatDomain QueryChannelsController")

    internal fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest {
        return QueryChannelsPaginationRequest(
            sort,
            channelOffset,
            channelLimit,
            messageLimit,
            memberLimit
        )
    }

    internal suspend fun updateQueryChannelSpec(channel: Channel) {
        if (newChannelEventFilter(channel, filter)) {
            addChannelToQueryChannelsSpec(channel)
        } else {
            removeChannelFromQueryChannelsSpec(channel.cid)
        }
    }

    internal fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            domainImpl.scope.launch { handleEvent(event) }
        }
    }

    internal suspend fun handleEvent(event: ChatEvent) {
        when (event) {
            is NotificationAddedToChannelEvent -> event.channel
            is ChannelUpdatedEvent -> event.channel
            is ChannelUpdatedByUserEvent -> event.channel
            is NotificationMessageNewEvent -> event.channel
            else -> null
        }?.let { updateQueryChannelSpec(it) }

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
            //  ChannelController.toChannel() can read the old version of the data using value
            // Solutions:
            // - suspend/wait for a few seconds (yuck, lets not do that)
            // - post the refresh on a flow object with only channel ids, and transform that into channels (this ensures it will get called after postValue completes)
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
        _channels.value += queryChannelsSpec.cids
            .intersect(cIds)
            .map { it to domainImpl.channel(it).toChannel() }
            .toMap()
    }

    internal suspend fun loadMore(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
    ): Result<List<Channel>> {
        val oldChannels = _channels.value.values
        val pagination = loadMoreRequest(channelLimit, messageLimit)
        return runQuery(pagination).map { it - oldChannels }
    }

    private suspend fun addChannelToQueryChannelsSpec(channel: Channel) =
        addChannelsToQueryChannelsSpec(listOf(channel))

    internal suspend fun removeChannelFromQueryChannelsSpec(cid: String) =
        removeChannelsFromQueryChannelsSpec(listOf(cid))

    private suspend fun addChannelsToQueryChannelsSpec(channels: List<Channel>) {
        queryChannelsSpec.cids += channels.map { it.cid }
        domainImpl.repos.insertQueryChannels(queryChannelsSpec)
        _channels.value = _channels.value + channels.map { it.cid to it }
    }

    private suspend fun removeChannelsFromQueryChannelsSpec(cids: List<String>) {
        queryChannelsSpec.cids = queryChannelsSpec.cids - cids
        domainImpl.repos.insertQueryChannels(queryChannelsSpec)
        _channels.value = _channels.value - cids
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
            addChannelsToQueryChannelsSpec(offlineChannels)
            loading.value = offlineChannels.isEmpty()
        }

        val output: Result<List<Channel>> = queryOnlineJob.await().let { onlineResult ->
            if (onlineResult.isSuccess) {
                onlineResult.also { updateOnlineChannels(it.data(), pagination.isFirstPage) }
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
        channelOffset = INITIAL_CHANNEL_OFFSET
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

    internal suspend fun updateOnlineChannel(channel: Channel) = updateOnlineChannels(listOf(channel), false)

    /**
     * Updates the state on the channelController based on the channel object we received from the API
     *
     * @param channels the list of channels to update
     * @param isFirstPage if it's the first page we set/replace the list of results. if it's not the first page we add to the list
     *
     */
    internal suspend fun updateOnlineChannels(
        channels: List<Channel>,
        isFirstPage: Boolean,
    ) {
        if (isFirstPage) {
            (_channels.value - channels.map { it.cid }).values
                .filterNot { newChannelEventFilter(it, filter) }
                .map { it.cid }
                .let { removeChannelsFromQueryChannelsSpec(it) }
        }
        channelOffset += channels.size
        channels.forEach { domainImpl.channel(it).updateDataFromChannel(it) }
        addChannelsToQueryChannelsSpec(channels)
    }

    internal suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(filter, domainImpl.userPresence)
        // next run the actual query
        val response = client.queryChannels(request).await()

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
            domainImpl.storeStateForChannels(channelsResponse)
        } else {
            logger.logI("Query with filter $filter failed, marking it as recovery needed")
            recoveryNeeded = true
            domainImpl.addError(response.error())
        }
        return response
    }

    internal suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        val query = domainImpl.repos.selectById(queryChannelsSpec.id)
            ?: return null

        return domainImpl.selectAndEnrichChannels(query.cids.toList(), pagination).also {
            logger.logI("found ${it.size} channels in offline storage")
        }
    }

    private fun List<ChannelMute>.toChannelsId() = map { channelMute -> channelMute.channel.id }

    public sealed class ChannelsState {
        /** The QueryChannelsController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         * */
        public object NoQueryActive : ChannelsState()

        /** Indicates we are loading the first page of results.
         * We are in this state if QueryChannelsController.loading is true
         * For seeing if we're loading more results have a look at QueryChannelsController.loadingMore
         *
         * @see QueryChannelsController.loadingMore
         * @see QueryChannelsController.loading
         * */
        public object Loading : ChannelsState()

        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : ChannelsState()

        /** The list of channels, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.online
         * */
        public data class Result(val channels: List<Channel>) : ChannelsState()
    }
}
