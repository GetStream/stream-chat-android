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
 
package io.getstream.chat.android.offline.plugin.state.querychannels.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.offline.event.handler.chat.DefaultChatEventHandler
import io.getstream.chat.android.offline.extensions.internal.updateUsers
import io.getstream.chat.android.offline.model.querychannels.internal.QueryChannelsSpec
import io.getstream.chat.android.offline.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class QueryChannelsMutableState(
    override val filter: FilterObject,
    override val sort: QuerySort<Channel>,
    scope: CoroutineScope,
    latestUsers: StateFlow<Map<String, User>>,
) : QueryChannelsState {

    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter, sort)
    internal val _channels = MutableStateFlow<Map<String, Channel>>(emptyMap())
    internal val _loading = MutableStateFlow(false)
    internal val _loadingMore = MutableStateFlow(false)
    internal val _endOfChannels = MutableStateFlow(false)
    private val _sortedChannels =
        _channels.combine(latestUsers) { channelMap, userMap ->
            channelMap.values.updateUsers(userMap)
        }
            .map { it.sortedWith(sort.comparator) }
            .stateIn(scope, SharingStarted.Eagerly, emptyList())
    internal val _currentRequest = MutableStateFlow<QueryChannelsRequest?>(null)
    internal val _recoveryNeeded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    internal val channelsOffset: MutableStateFlow<Int> = MutableStateFlow(0)

    /** Instance of [ChatEventHandler] that handles logic of event handling for this [QueryChannelsMutableState]. */
    override var chatEventHandler: ChatEventHandler? = null

    override val recoveryNeeded: StateFlow<Boolean> = _recoveryNeeded

    /**
     * Non-nullable property of [ChatEventHandler] to ensure we always have some handler to handle events. Returns
     * handler set by user or default one if there is no.
     */
    internal val eventHandler: ChatEventHandler
        get() = chatEventHandler ?: DefaultChatEventHandler(_sortedChannels)

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest
    override val loading: StateFlow<Boolean> = _loading
    override val loadingMore: StateFlow<Boolean> = _loadingMore
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels
    override val channels: StateFlow<List<Channel>> = _sortedChannels
    override val channelsStateData: StateFlow<ChannelsStateData> =
        _loading.combine(_sortedChannels) { loading: Boolean, channels: List<Channel> ->
            when {
                loading -> ChannelsStateData.Loading
                channels.isEmpty() -> ChannelsStateData.OfflineNoResults
                else -> ChannelsStateData.Result(channels)
            }
        }.stateIn(scope, SharingStarted.Eagerly, ChannelsStateData.NoQueryActive)

    override val nextPageRequest: StateFlow<QueryChannelsRequest?> =
        currentRequest.combine(channelsOffset) { currentRequest, currentOffset ->
            currentRequest?.copy(offset = currentOffset)
        }.stateIn(scope, SharingStarted.Eagerly, null)
}

internal fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState
