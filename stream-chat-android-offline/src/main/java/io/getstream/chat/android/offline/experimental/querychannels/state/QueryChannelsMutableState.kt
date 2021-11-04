package io.getstream.chat.android.offline.experimental.querychannels.state

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.querychannels.ChannelEventsHandler
import io.getstream.chat.android.offline.querychannels.DefaultChannelEventsHandler
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@ExperimentalStreamChatApi
internal class QueryChannelsMutableState(
    override val filter: FilterObject,
    override val sort: QuerySort<Channel>,
    client: ChatClient,
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
    internal val recoveryNeeded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    internal val channelsOffset: MutableStateFlow<Int> = MutableStateFlow(0)

    internal var defaultChannelEventsHandler: DefaultChannelEventsHandler = DefaultChannelEventsHandler(client, _sortedChannels)
    override var checkFilterOnChannelUpdatedEvent: Boolean by defaultChannelEventsHandler::checkFilterOnChannelUpdatedEvent
    override var newChannelEventFilter: suspend (Channel, FilterObject) -> Boolean by defaultChannelEventsHandler::newChannelEventFilter

    override var channelEventsHandler: ChannelEventsHandler? = null

    internal val eventsHandler: ChannelEventsHandler
        get() = channelEventsHandler ?: defaultChannelEventsHandler

    override val currentRequest: StateFlow<QueryChannelsRequest?> = _currentRequest
    override val loading: StateFlow<Boolean> = _loading
    override val loadingMore: StateFlow<Boolean> = _loadingMore
    override val endOfChannels: StateFlow<Boolean> = _endOfChannels
    override val channels: StateFlow<List<Channel>> = _sortedChannels
    override val mutedChannelIds: StateFlow<List<String>> = _mutedChannelIds
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

@ExperimentalStreamChatApi
internal fun QueryChannelsState.toMutableState(): QueryChannelsMutableState = this as QueryChannelsMutableState
