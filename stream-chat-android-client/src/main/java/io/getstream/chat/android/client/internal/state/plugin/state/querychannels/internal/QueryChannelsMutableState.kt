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

package io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal

import io.getstream.chat.android.client.api.event.ChatEventHandler
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.client.api.event.EventHandlingResult
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.ChannelsStateData
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.internal.updateLiveLocations
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Mutable backing state for a query channels operation. Each instance corresponds to a unique
 * [QueryChannelsIdentifier] (Standard or Predefined).
 *
 * For [QueryChannelsIdentifier.Standard], `initialFilter`/`initialSort` come from the client and
 * are immutable across the lifetime of this state — [applyResolvedSpec] is effectively a no-op.
 *
 * For [QueryChannelsIdentifier.Predefined], `initialFilter`/`initialSort` are placeholders
 * (defaults supplied by the registry) until [applyResolvedSpec] is called either with the
 * server-resolved values from `QueryChannelsResult.predefinedFilter` or with values rehydrated
 * from the offline DB. The internal `_sort` flow drives the sorted channel list, so re-sorting
 * happens automatically once the resolved sort is applied.
 */
internal class QueryChannelsMutableState(
    val identifier: QueryChannelsIdentifier,
    initialFilter: FilterObject,
    initialSort: QuerySorter<Channel>,
    scope: CoroutineScope,
    latestUsers: StateFlow<Map<String, User>>,
    activeLiveLocations: StateFlow<List<Location>>,
) : QueryChannelsState {

    private val _filter: MutableStateFlow<FilterObject> = MutableStateFlow(initialFilter)
    private val _sort: MutableStateFlow<QuerySorter<Channel>> = MutableStateFlow(initialSort)

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
     * In-memory cache spec for the active query.
     */
    private var _querySpec: QueryChannelsSpec = when (identifier) {
        is QueryChannelsIdentifier.Standard -> QueryChannelsSpec.create(
            filter = initialFilter,
            querySort = initialSort,
        )
        is QueryChannelsIdentifier.Predefined -> QueryChannelsSpec.create(
            filter = initialFilter,
            querySort = initialSort,
            predefinedFilterName = identifier.name,
            predefinedFilterValues = identifier.filterValues,
            predefinedSortValues = identifier.sortValues,
        )
    }
    internal val queryChannelsSpec: QueryChannelsSpec
        get() = _querySpec

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
    private val sortedChannels: StateFlow<List<Channel>?> =
        combine(mapChannels, latestUsers, activeLiveLocations, _sort) { channelMap, userMap, activeLocations, sort ->
            channelMap?.values
                ?.updateUsers(userMap)
                ?.updateLiveLocations(activeLocations)
                ?.sortedWith(sort.comparator)
        }.stateIn(scope, SharingStarted.Eagerly, null)
    private var _currentRequest: MutableStateFlow<QueryChannelsRequest?>? = MutableStateFlow(null)
    private var _recoveryNeeded: MutableStateFlow<Boolean>? = MutableStateFlow(false)
    private var _channelsOffset: MutableStateFlow<Int>? = MutableStateFlow(0)
    internal val channelsOffset: StateFlow<Int> = _channelsOffset!!

    override var chatEventHandlerFactory: ChatEventHandlerFactory? = null

    override val recoveryNeeded: StateFlow<Boolean> = _recoveryNeeded!!

    /**
     * Non-nullable property of [ChatEventHandler] to ensure we always have some handler to handle events. Returns
     * handler set by user or default one if there is no.
     */
    private val eventHandler: ChatEventHandler by lazy {
        (chatEventHandlerFactory ?: ChatEventHandlerFactory()).chatEventHandler(mapChannels)
    }

    fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        // TODO: Think about this part: Are filters enough, or should we sent the spec as well?
        return eventHandler.handleChatEvent(event, filter, cachedChannel)
    }

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingMore: StateFlow<Boolean> = _loadingMore!!
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels!!
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
     * @parami isEnd Boolean
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
     * Applies the resolved filter/sort to the state. Called for predefined-filter queries when:
     *  - The server response arrives carrying `QueryChannelsResult.predefinedFilter`, or
     *  - The offline DB rehydrates a previously persisted resolved spec for the same identifier.
     *
     * Not relevant for standard (non-predefined) queries.
     *
     * Because [QueryChannelsSpec] keeps `filter` and `querySort` as `val` for binary
     * compatibility, we replace the held [_querySpec] instance instead of mutating it in place.
     * `cids` and the predefined identity fields are carried over from the previous instance.
     */
    fun applyResolvedSpec(filter: FilterObject, sort: QuerySorter<Channel>) {
        _filter.value = filter
        _sort.value = sort
        val previous = _querySpec
        _querySpec = QueryChannelsSpec.create(
            filter = filter,
            querySort = sort,
            cids = previous.cids,
            predefinedFilterName = previous.predefinedFilterName,
            predefinedFilterValues = previous.predefinedFilterValues,
            predefinedSortValues = previous.predefinedSortValues,
        )
    }

    fun destroy() {
        _channels = null
        _loading = null
        _loadingMore = null
        _endOfChannels = null
        _currentRequest = null
        _recoveryNeeded = null
        _channelsOffset = null
    }
}

internal fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState
