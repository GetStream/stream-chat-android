/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class QueryChannelsMutableState(
    override val filter: FilterObject,
    override val sort: QuerySorter<Channel>,
    scope: CoroutineScope,
    latestUsers: StateFlow<Map<String, User>>,
    activeLiveLocations: StateFlow<List<Location>>,
) : QueryChannelsState {

    private val logger by taggedLogger("Chat:QueryChannelsState")

    internal var rawChannels: Map<String, Channel>?
        get() = _channels?.value
        private set(value) {
            println("JcLog: setting rawChannels: $value")
            _channels?.value = value
        }

    // This is needed for queries
    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter, sort)

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
        combine(mapChannels, latestUsers, activeLiveLocations) { channelMap, userMap, activeLocations ->
            channelMap?.values
                ?.updateUsers(userMap)
                ?.updateLiveLocations(activeLocations)
        }.map { channels ->
            channels?.sortedWith(sort.comparator)
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
        return eventHandler.handleChatEvent(event, filter, cachedChannel)
    }

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest!!
    override val loading: StateFlow<Boolean> = _loading!!
    override val loadingMore: StateFlow<Boolean> = _loadingMore!!
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels!!
    override val channels: StateFlow<List<Channel>?> = sortedChannels
    override val channelsStateData: StateFlow<ChannelsStateData> =
        loading.combine(sortedChannels) { loading: Boolean, channels: List<Channel>? ->
            println("JcLog: channelStateData: $channels")
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

    fun setChannels(channelsMap: Map<String, Channel>) {
        rawChannels = channelsMap
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
