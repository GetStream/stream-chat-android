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
import io.getstream.chat.android.client.api.models.querysort.QuerySorter
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.extensions.internal.updateUsers
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.logging.StreamLog
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
) : QueryChannelsState {

    private val logger = StreamLog.getLogger("Chat:QueryChannelsState")

    internal var rawChannels: Map<String, Channel>
        get() = _channels.value
        private set(value) {
            _channels.value = value
        }

    // This is needed for queries
    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter, sort)
    private val _channels = MutableStateFlow<Map<String, Channel>>(emptyMap())
    private val _loading = MutableStateFlow(false)
    private val _loadingMore = MutableStateFlow(false)

    internal val currentLoading: MutableStateFlow<Boolean>
        get() = if (channels.value.isNullOrEmpty()) _loading else _loadingMore

    private val _endOfChannels = MutableStateFlow(false)
    private val _sortedChannels: StateFlow<List<Channel>?> =
        _channels.combine(latestUsers) { channelMap, userMap ->
            channelMap.values.updateUsers(userMap)
        }.map { channels ->
            if (channels.isNotEmpty()) {
                logger.d {
                    val ids = channels.joinToString { channel -> channel.id }
                    "Sorting channels: $ids"
                }
            }

            channels.sortedWith(sort.comparator).also { sortedChannels ->
                if (sortedChannels.isNotEmpty()) {
                    logger.d {
                        val ids = sortedChannels.joinToString { channel -> channel.id }
                        "Sorting result: $ids"
                    }
                }
            }
        }.stateIn(scope, SharingStarted.Eagerly, null)
    private val _currentRequest = MutableStateFlow<QueryChannelsRequest?>(null)
    private val _recoveryNeeded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    internal val channelsOffset: MutableStateFlow<Int> = MutableStateFlow(0)

    override var chatEventHandlerFactory: ChatEventHandlerFactory? = null

    override val recoveryNeeded: StateFlow<Boolean> = _recoveryNeeded

    /**
     * Non-nullable property of [ChatEventHandler] to ensure we always have some handler to handle events. Returns
     * handler set by user or default one if there is no.
     */
    private val eventHandler: ChatEventHandler by lazy {
        (chatEventHandlerFactory ?: ChatEventHandlerFactory()).chatEventHandler(_channels)
    }

    fun handleChatEvent(event: ChatEvent, cachedChannel: Channel?): EventHandlingResult {
        return eventHandler.handleChatEvent(event, filter, cachedChannel)
    }

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest
    override val loading: StateFlow<Boolean> = _loading
    override val loadingMore: StateFlow<Boolean> = _loadingMore
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels
    override val channels: StateFlow<List<Channel>?> = _sortedChannels
    override val channelsStateData: StateFlow<ChannelsStateData> =
        _loading.combine(_sortedChannels) { loading: Boolean, channels: List<Channel>? ->
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
        _loadingMore.value = isLoading
    }

    /**
     * Set loading more. Notifies if the SDK is loading the first page.
     */
    fun setLoadingFirstPage(isLoading: Boolean) {
        _loading.value = isLoading
    }

    /**
     * Set the current request being made.
     *
     * @param request [QueryChannelsRequest]
     */
    fun setCurrentRequest(request: QueryChannelsRequest) {
        _currentRequest.value = request
    }

    /**
     * Set the end of channels.
     *
     * @parami isEnd Boolean
     */
    fun setEndOfChannels(isEnd: Boolean) {
        _endOfChannels.value = isEnd
    }

    /**
     * Sets if recovery is needed.
     *
     * @param recoveryNeeded Boolean
     */
    fun setRecoveryNeeded(recoveryNeeded: Boolean) {
        _recoveryNeeded.value = recoveryNeeded
    }

    /**
     * Set the offset of the channels.
     *
     * @param offset Int
     */
    fun setChannelsOffset(offset: Int) {
        channelsOffset.value = offset
    }

    fun setChannels(channelsMap: Map<String, Channel>) {
        rawChannels = channelsMap
    }
}

internal fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState
