package io.getstream.chat.android.offline.querychannels.state

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
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
) : QueryChannelsState {

    internal val queryChannelsSpec: QueryChannelsSpec = QueryChannelsSpec(filter)

    internal val _channels = MutableStateFlow<Map<String, Channel>>(emptyMap())
    internal val _loading = MutableStateFlow(false)
    internal val _loadingMore = MutableStateFlow(false)
    internal val _endOfChannels = MutableStateFlow(false)
    internal val _sortedChannels =
        _channels.map { it.values.sortedWith(sort.comparator) }.stateIn(scope, SharingStarted.Eagerly, emptyList())
    internal val _mutedChannelIds = MutableStateFlow<List<String>>(emptyList())
    internal val _currentRequest = MutableStateFlow<QueryChannelsRequest?>(null)

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest
    override val loading: StateFlow<Boolean> = _loading
    override val loadingMore: StateFlow<Boolean> = _loadingMore
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels
    override val channels: StateFlow<List<Channel>> = _sortedChannels
    override val mutedChannelIds: StateFlow<List<String>> = _mutedChannelIds
    override val channelsState: StateFlow<ChannelsState> =
        _loading.combine(_sortedChannels) { loading: Boolean, channels: List<Channel> ->
            when {
                loading -> ChannelsState.Loading
                channels.isEmpty() -> ChannelsState.OfflineNoResults
                else -> ChannelsState.Result(channels)
            }
        }.stateIn(scope, SharingStarted.Eagerly, ChannelsState.NoQueryActive)
}

internal fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState
