package io.getstream.chat.android.compose.viewmodel.channels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull

internal interface IChannelViewState {
    val channelsState: ChannelsState
    var queryChannelsState: StateFlow<QueryChannelsState?>
    val channelMutes: StateFlow<List<ChannelMute>>
    val searchQuery: MutableStateFlow<SearchQuery>
    val searchMessageState: MutableStateFlow<SearchMessageState?>
    val filterFlow: MutableStateFlow<FilterObject?>
    val querySortFlow: MutableStateFlow<QuerySorter<Channel>>
    val queryConfigFlow: Flow<QueryConfig<Channel>>

    fun updateChannelState(channelsState: ChannelsState)
}

internal class ChannelViewStateImpl(
    chatClient: ChatClient,
    initialSort: QuerySorter<Channel>,
    initialFilters: FilterObject?,
) : IChannelViewState {
    /**
     * The current state of the channels screen. It holds all the information required to render the UI.
     */
    override var channelsState: ChannelsState by mutableStateOf(ChannelsState())
        private set

    override fun updateChannelState(channelsState: ChannelsState) {
        this.channelsState = channelsState
    }

    /**
     * Current query channels state that contains filter, sort and other states related to channels query.
     */
    override var queryChannelsState: StateFlow<QueryChannelsState?> = MutableStateFlow(null)

    /**
     * Gives us the information about the list of channels mutes by the current user.
     */
    override val channelMutes: StateFlow<List<ChannelMute>> = chatClient.globalState.channelMutes

    /**
     * The current state of the search input. When changed, it emits a new value in a flow, which
     * queries and loads new data.
     */
    override val searchQuery: MutableStateFlow<SearchQuery> = MutableStateFlow<SearchQuery>(SearchQuery.Empty)

    /**
     * The current state of the search Messages. When changed, it emits a new value in a flow, which
     * queries and loads new data.
     */
    private val searchMessageStateInternal: MutableStateFlow<SearchMessageState?> = MutableStateFlow(null)

    override val searchMessageState: MutableStateFlow<SearchMessageState?> = searchMessageStateInternal

    /**
     * State flow that keeps the value of the current [FilterObject] for channels.
     */
    override val filterFlow: MutableStateFlow<FilterObject?> = MutableStateFlow(initialFilters)

    /**
     * State flow that keeps the value of the current [QuerySorter] for channels.
     */
    override val querySortFlow: MutableStateFlow<QuerySorter<Channel>> = MutableStateFlow(initialSort)

    /**
     * The currently active query configuration, stored in a [MutableStateFlow]. It's created using
     * the `initialFilters` parameter and the initial sort, but can be changed.
     */
    override val queryConfigFlow: Flow<QueryConfig<Channel>> =
        filterFlow.filterNotNull().combine(querySortFlow) { filters, sort ->
            QueryConfig(filters = filters, querySort = sort)
        }
}