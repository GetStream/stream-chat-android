/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.state.plugin.state.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.internal.updateLiveLocations
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.event.handler.grouped.internal.group
import io.getstream.chat.android.state.event.handler.grouped.internal.groupedLocalSort
import io.getstream.chat.android.state.event.handler.grouped.internal.matchesGroupedChannel
import io.getstream.chat.android.state.event.handler.grouped.internal.queryTimestamp
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.GroupedQueryConfig
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import java.util.Date

/**
 * Mutable backing state for a query channels operation. Each instance corresponds to a unique
 * [QueryChannelsIdentifier] (Standard, Predefined, or Grouped). Initial spec, filter, and sort
 * are derived from the identifier — callers only pass the identifier itself.
 *
 * For [QueryChannelsIdentifier.Standard], `filter`/`sort` come from the identifier and are
 * immutable across the lifetime of this state — [applyResolvedSpec] is a no-op.
 *
 * For [QueryChannelsIdentifier.Predefined], `filter`/`sort` start as neutral placeholders until
 * [applyResolvedSpec] is called either with the server-resolved values from
 * `QueryChannelsResult.predefinedFilter` or with values rehydrated from the offline DB. The
 * internal `_sort` flow drives the sorted channel list, so re-sorting happens automatically once
 * the resolved sort is applied.
 *
 * For [QueryChannelsIdentifier.Grouped], `filter` starts neutral and `sort` starts as
 * `last_updated` descending; both follow the group's latest request through [setGroupedQueryConfig].
 * Channels are populated via the listener-driven grouped-channels endpoint, and the cursor lives on
 * [_nextCursor].
 */
internal class QueryChannelsMutableState(
    val identifier: QueryChannelsIdentifier,
    scope: CoroutineScope,
    latestUsers: StateFlow<Map<String, User>>,
    activeLiveLocations: StateFlow<List<Location>>,
    private val now: () -> Date = ::Date,
) : QueryChannelsState {

    /**
     * In-memory cache spec for the active query. Carries variant-specific identity fields
     * (`groupKey` for Grouped, `predefinedFilter*` for Predefined) so they survive
     * [QueryChannelsSpec] round-trips and DB persistence.
     */
    private var _querySpec: QueryChannelsSpec = when (identifier) {
        is QueryChannelsIdentifier.Standard -> QueryChannelsSpec(
            filter = identifier.filter,
            querySort = identifier.sort,
        )
        is QueryChannelsIdentifier.Predefined -> QueryChannelsSpec(
            filter = Filters.neutral(),
            querySort = QuerySortByField(),
            predefinedFilterName = identifier.name,
            predefinedFilterValues = identifier.filterValues,
            predefinedSortValues = identifier.sortValues,
        )
        is QueryChannelsIdentifier.Grouped -> QueryChannelsSpec(
            filter = Filters.neutral(),
            querySort = DEFAULT_GROUPED_SORT,
            groupKey = identifier.groupKey,
        )
    }

    /** Spec backing this state. [QueryChannelsSpec.cids] is mutated in place via [setCids]. */
    internal val queryChannelsSpec: QueryChannelsSpec
        get() = _querySpec

    private val _filter: MutableStateFlow<FilterObject> = MutableStateFlow(_querySpec.filter)
    private val _sort: MutableStateFlow<QuerySorter<Channel>> = MutableStateFlow(_querySpec.querySort)

    override val filter: FilterObject
        get() = _filter.value
    override val sort: QuerySorter<Channel>
        get() = _sort.value

    internal var rawChannels: Map<String, Channel>?
        get() = _channels?.value
        private set(value) {
            _channels?.value = value
        }

    /**
     * Property that exposes a map of raw channels.
     * The channels are later sorted and enriched with latest users updates
     * and exposed either as [channels] or [channelsStateData].
     * The value is nullable in order to have a clear distinction between different channels state. When the value is:
     * - null - the state should be either [ChannelsStateData.NoQueryActive] or [ChannelsStateData.Loading]
     * - emptyMap() - the stat should be [ChannelsStateData.OfflineNoResults]
     * - notEmptyMap() - the state should be [ChannelsStateData.Result]
     */
    private var _channels: MutableStateFlow<Map<String, Channel>?>? = MutableStateFlow(null)
    private val mapChannels: StateFlow<Map<String, Channel>?> = _channels!!
    private var _loading: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _loadingMore: MutableStateFlow<Boolean>? = MutableStateFlow(false)

    internal val currentLoading: StateFlow<Boolean>
        get() = if (channels.value.isNullOrEmpty()) loading else loadingMore

    private var _endOfChannels: MutableStateFlow<Boolean>? = MutableStateFlow(false)

    /**
     * Which channels of a filtered group are shown. Re-checked every [GROUPED_FILTER_CHECK_INTERVAL_MS], because a
     * channel can pass its `query_timestamp` cutoff without any event.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val isShown: Flow<(Channel) -> Boolean> = _filter.flatMapLatest { filter ->
        if (identifier !is QueryChannelsIdentifier.Grouped || filter is NeutralFilterObject) {
            flowOf { true }
        } else {
            flow {
                while (true) {
                    val checkedAt = now()
                    emit { channel: Channel ->
                        filter.matchesGroupedChannel(channel.group, channel.queryTimestamp, checkedAt)
                    }
                    delay(GROUPED_FILTER_CHECK_INTERVAL_MS)
                }
            }
        }
    }

    private val sortedChannels: StateFlow<List<Channel>?> =
        combine(
            mapChannels,
            latestUsers,
            activeLiveLocations,
            _sort,
            isShown,
        ) { channelMap, userMap, activeLocations, sort, isShown ->
            channelMap?.values
                ?.filter(isShown)
                ?.updateUsers(userMap)
                ?.updateLiveLocations(activeLocations)
                ?.sortedWith(sort.comparator)
        }.stateIn(scope, SharingStarted.Eagerly, null)
    private var _currentRequest: MutableStateFlow<QueryChannelsRequest?>? = MutableStateFlow(null)
    private var _recoveryNeeded: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _channelsOffset: MutableStateFlow<Int>? = MutableStateFlow(0)
    internal val channelsOffset: StateFlow<Int> = _channelsOffset!!

    private var _nextCursor: MutableStateFlow<String?>? = MutableStateFlow(null)
    private var _groupedQueryConfig: MutableStateFlow<GroupedQueryConfig?>? = MutableStateFlow(null)
    private val _groupedUnreadBase = MutableStateFlow<Int?>(null)

    /**
     * For a filtered group, the unread channels the server counted that are not loaded in [channels]. `null` when
     * the group has no filter, so its badge keeps the count the server sends with events.
     */
    val groupedUnreadBase: StateFlow<Int?> = _groupedUnreadBase

    override var chatEventHandlerFactory: ChatEventHandlerFactory? = null
        set(value) {
            field = value
            _eventHandler = value?.chatEventHandler(mapChannels)
        }

    override val recoveryNeeded: StateFlow<Boolean> = _recoveryNeeded!!

    /**
     * Non-nullable property of [ChatEventHandler] to ensure we always have some handler to handle events. Returns
     * handler set by user or default one if there is no.
     * Re-created when [chatEventHandlerFactory] changes.
     */
    private var _eventHandler: ChatEventHandler? = null
    private val eventHandler: ChatEventHandler
        get() = _eventHandler ?: ChatEventHandlerFactory().chatEventHandler(mapChannels)

    fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return eventHandler.handleChatEvent(event, filter, cachedChannel)
    }

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingMore: StateFlow<Boolean> = _loadingMore!!
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels!!
    override val nextCursor: StateFlow<String?> = _nextCursor!!
    override val groupedQueryConfig: StateFlow<GroupedQueryConfig?> = _groupedQueryConfig!!
    override val channels: StateFlow<List<Channel>?> = sortedChannels
    override val channelsStateData: StateFlow<ChannelsStateData> =
        loading.combine(sortedChannels) { loading: Boolean, channels: List<Channel>? ->
            when {
                loading || channels == null -> ChannelsStateData.Loading
                channels.isEmpty() -> ChannelsStateData.OfflineNoResults
                else -> ChannelsStateData.Result(channels)
            }
        }.stateIn(scope, SharingStarted.Eagerly, ChannelsStateData.NoQueryActive)

    override val nextPageRequest: StateFlow<QueryChannelsRequest?> =
        currentRequest.combine(channelsOffset) { currentRequest, currentOffset ->
            currentRequest?.copy(offset = currentOffset)
        }.stateIn(scope, SharingStarted.Eagerly, null)

    /**
     * Set loading more. Notifies if the SDK is loading more channels.
     */
    fun setLoadingMore(isLoading: Boolean) {
        _loadingMore?.value = isLoading
    }

    /**
     * Set loading more. Notifies if the SDK is loading the first page.
     */
    fun setLoadingFirstPage(isLoading: Boolean) {
        _loading?.value = isLoading
    }

    /**
     * Set the current request being made.
     *
     * @param request [QueryChannelsRequest]
     */
    fun setCurrentRequest(request: QueryChannelsRequest) {
        _currentRequest?.value = request
    }

    /**
     * Set the end of channels.
     *
     * @param isEnd Boolean
     */
    fun setEndOfChannels(isEnd: Boolean) {
        _endOfChannels?.value = isEnd
    }

    /**
     * Sets if recovery is needed.
     *
     * @param recoveryNeeded Boolean
     */
    fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        _recoveryNeeded?.value = recoveryNeeded
    }

    /**
     * Set the offset of the channels.
     *
     * @param offset Int
     */
    fun setChannelsOffset(offset: Int) {
        _channelsOffset?.value = offset
    }

    /**
     * Replaces the current channel map with a new one.
     *
     * @param channelsMap The new map holding pairs of CID -> Channel.
     */
    fun setChannels(channelsMap: Map<String, Channel>) {
        rawChannels = channelsMap
    }

    /**
     * Set the next-page cursor. Used by the grouped-channels path; the standard and predefined
     * paths don't publish a cursor here.
     */
    fun setNextCursor(cursor: String?) {
        _nextCursor?.value = cursor
    }

    /**
     * Store the configuration that produced the current page of grouped results. Read back by
     * [io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel] for paginated
     * calls and by [io.getstream.chat.android.state.sync.internal.SyncManager] for recovery. The
     * group's filter becomes this state's [filter], which event handling checks channels against.
     * The filter and sort are also kept in [queryChannelsSpec], so they are saved with the group.
     */
    fun setGroupedQueryConfig(config: GroupedQueryConfig) {
        isGroupedQueryConfigRestored = false
        _groupedQueryConfig?.value = config
        val filter = config.filter ?: Filters.neutral()
        _filter.value = filter
        if (filter == Filters.neutral()) _groupedUnreadBase.value = null
        _sort.value = groupedLocalSort(config.sort, DEFAULT_GROUPED_SORT)
        // The 2-arg copy drops cids; keep them, since a paginated result appends to them.
        val cids = _querySpec.cids
        _querySpec = _querySpec.copy(filter = filter, querySort = config.sort ?: DEFAULT_GROUPED_SORT)
            .also { it.cids = cids }
    }

    /** Sets [groupedUnreadBase]. */
    fun setGroupedUnreadBase(base: Int?) {
        _groupedUnreadBase.value = base
    }

    /**
     * Restores the filter and sort saved with the group by an earlier session, so the cached list is
     * filtered before the first request. No-op when a request has already set the configuration, or
     * when the group was saved without a filter or sort.
     */
    fun restoreGroupedQueryConfig(filter: FilterObject, sort: QuerySorter<Channel>) {
        if (_groupedQueryConfig?.value != null) return
        val hasFilter = filter != Filters.neutral()
        val hasSort = sort != DEFAULT_GROUPED_SORT
        if (!hasFilter && !hasSort) return
        setGroupedQueryConfig(
            GroupedQueryConfig(
                limit = null,
                pageSize = null,
                watch = true,
                presence = false,
                filter = filter.takeIf { hasFilter },
                sort = sort.takeIf { hasSort },
            ),
        )
        isGroupedQueryConfigRestored = true
    }

    /**
     * Whether [groupedQueryConfig] was restored from the cache rather than set by a request. A restored
     * configuration only has defaults for its request-level fields (`limit`, `watch`, `presence`).
     */
    @Volatile
    var isGroupedQueryConfigRestored: Boolean = false
        private set

    /**
     * Applies the resolved filter/sort to the state. Only relevant for predefined-filter queries,
     * where the actual filter/sort are not known until either:
     *  - The server response arrives carrying `QueryChannelsResult.predefinedFilter`, or
     *  - The offline DB rehydrates a previously persisted resolved spec for the same identifier.
     *
     * No-op for [QueryChannelsIdentifier.Standard] queries, whose filter/sort are fixed at construction
     * time, and for [QueryChannelsIdentifier.Grouped] queries, whose filter/sort come from
     * [setGroupedQueryConfig].
     *
     * The 2-arg [QueryChannelsSpec.copy] preserves variant-specific identity fields (predefined
     * name/values, groupKey) and `cids` from the previous spec instance.
     */
    fun applyResolvedSpec(filter: FilterObject, sort: QuerySorter<Channel>) {
        if (identifier !is QueryChannelsIdentifier.Predefined) return
        _filter.value = filter
        _sort.value = sort
        _querySpec = _querySpec.copy(filter = filter, querySort = sort)
    }

    /** Updates [QueryChannelsSpec.cids] on the held spec. */
    internal fun setCids(cids: Set<String>) {
        _querySpec.cids = cids
    }

    fun destroy() {
        _channels = null
        _loading = null
        _loadingMore = null
        _endOfChannels = null
        _currentRequest = null
        _recoveryNeeded = null
        _channelsOffset = null
        _nextCursor = null
        _groupedQueryConfig = null
    }
}

internal fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState

private const val GROUPED_FILTER_CHECK_INTERVAL_MS = 60_000L

private val DEFAULT_GROUPED_SORT = QuerySortByField.descByName<Channel>("last_updated")
