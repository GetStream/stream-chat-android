package io.getstream.chat.android.compose.viewmodel.channel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.state.channel.list.Cancel
import io.getstream.chat.android.compose.state.channel.list.ChannelListAction
import io.getstream.chat.android.compose.state.channel.list.ChannelsState
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/**
 * A state store that represents all the information required to query, filter, show and react to
 * [Channel] items in a list.
 *
 * Exposes the following state:
 *
 * @property initialFilters - The current data filter. Users can change this state using [setFilters] to
 * impact which data is shown on the UI.
 *
 * @property searchQuery - The current channel query. Users can change it through
 * [onSearchChanged]. This impacts what data is shown on the UI.
 *
 * @property channelsState - The state of the ChannelsList, represented by [ChannelsState].
 *
 * @property selectedChannel - Currently selected channel, of which the users can view the info.
 * */
class ChannelListViewModel(
    val chatClient: ChatClient,
    val chatDomain: ChatDomain,
    initialSort: QuerySort<Channel>,
    private val initialFilters: FilterObject,
) : ViewModel() {

    /**
     * The currently active query configuration, stored in a [MutableStateFlow]. It's created using
     * the [initialFilters] and the initial sort, but can be changed.
     * */
    private val queryConfig = MutableStateFlow(QueryConfig(initialFilters, initialSort))

    /**
     * The current state of the search input. When changed, it emits a new value in a flow, which
     * queries for a new [QueryChannelsController] and loads new data.
     * */
    private val searchQuery = MutableStateFlow("")

    /**
     * The current state of the channels screen. It holds all the information required to render the UI.
     * */
    var channelsState by mutableStateOf(ChannelsState())
        private set

    /**
     * Currently selected channel, if any. Used to show the bottom drawer information when long
     * tapping on a list item.
     * */
    var selectedChannel: Channel? by mutableStateOf(null)
        private set

    /**
     * Currently active channel action, if any. Used to show a dialog for deleting or leaving a
     * channel/conversation.
     * */
    var activeChannelAction: ChannelListAction? by mutableStateOf(null)
        private set

    /**
     * The state of our network connection - if we're online or not.
     * */
    val isOnline: StateFlow<Boolean> = chatDomain.online

    /**
     * The state of the currently logged in user.
     * */
    val user: StateFlow<User?> = chatDomain.user

    /**
     * Combines the latest search query and filter to fetch channels and emit them to the UI.
     * */
    fun start() {
        viewModelScope.launch {
            searchQuery.combine(queryConfig) { query, config -> query to config }
                .collectLatest { (query, config) ->

                    // TODO we need to make these filters consistent
                    val filter =
                        if (query.isNotEmpty()) Filters.and(config.filters, Filters.eq("id", query)) else config.filters

                    val result = chatDomain.queryChannels(filter, config.querySort).await()

                    if (result.isSuccess) {
                        observeChannels(result.data())
                    } else {
                        result.error().cause?.printStackTrace()
                        channelsState =
                            channelsState.copy(isLoading = false, channels = emptyList())
                    }
                }
        }
    }

    /**
     * Kicks off operations required to combine and build the [ChannelsState] object for the UI.
     *
     * It connects the 'loadingMore', 'channelsState' and 'endOfChannels' properties from the [controller].
     * */
    private suspend fun observeChannels(controller: QueryChannelsController) {
        controller.loadingMore
            .combine(controller.channelsState) { isLoadingMore, channelsState ->
                isLoadingMore to channelsState
            }.combine(controller.endOfChannels) { (isLoadingMore, state), endOfChannels ->
                when (state) {
                    QueryChannelsController.ChannelsState.NoQueryActive,
                    QueryChannelsController.ChannelsState.Loading,
                    ->
                        channelsState.copy(isLoading = true)
                    QueryChannelsController.ChannelsState.OfflineNoResults -> channelsState.copy(
                        isLoading = false,
                        channels = emptyList()
                    )
                    is QueryChannelsController.ChannelsState.Result -> {
                        channelsState.copy(
                            isLoading = false,
                            channels = state.channels,
                            isLoadingMore = isLoadingMore,
                            endOfChannels = endOfChannels
                        )
                    }
                }
            }.collectLatest { newState -> channelsState = newState }
    }

    /**
     * Changes the currently selected channel state. This updates the UI state and allows us to observe
     * the state change.
     * */
    fun onChannelSelected(channel: Channel?) {
        this.selectedChannel = channel
    }

    /**
     * Changes the current query state. This updates the data flow and triggers another query operation.
     *
     * The new operation will hold the channels that match the new query.
     * */
    fun onSearchChanged(newQuery: String) {
        this.searchQuery.value = newQuery
    }

    /**
     * Allows for the change of filters used for channel queries.
     *
     * Use this if you need to support runtime filter changes, through custom filters UI.
     * */
    fun setFilters(newFilters: FilterObject) {
        val currentConfig = this.queryConfig.value

        this.queryConfig.value =
            currentConfig.copy(filters = Filters.and(initialFilters, newFilters))
    }

    /**
     * Allows for the change of the query sort used for channel queries.
     *
     * Use this if you need to support runtime sort changes, through custom sort UI.
     * */
    fun setQuerySort(querySort: QuerySort<Channel>) {
        val currentConfig = this.queryConfig.value

        this.queryConfig.value =
            currentConfig.copy(querySort = querySort)
    }

    /**
     * Loads more data when the user reaches the end of the channels list.
     * */
    fun loadMore() {
        val currentConfig = queryConfig.value
        val query = searchQuery.value

        chatDomain.queryChannelsLoadMore(
            Filters.and(currentConfig.filters, Filters.greaterThanEquals("id", query)),
            currentConfig.querySort
        ).enqueue()
    }

    /**
     * Clears the active action if we've chosen [Cancel], otherwise, stores the selected action as
     * the currently active action, in [activeChannelAction].
     *
     * It also removes the [selectedChannel] if the action is [Cancel].
     *
     * @param channelListAction - The selected action.
     * */
    fun onChannelAction(channelListAction: ChannelListAction) {
        if (channelListAction is Cancel) {
            selectedChannel = null
        }

        activeChannelAction = if (channelListAction == Cancel) {
            null
        } else {
            channelListAction
        }
    }

    /**
     * Deletes a channel, after the user chooses the delete [ChannelListAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel - The channel to delete.
     * */
    fun deleteConversation(channel: Channel) {
        dismissChannelAction()

        chatDomain.deleteChannel(channel.id).enqueue()
    }

    /**
     * Leaves a channel, after the user chooses the leave [ChannelListAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel - The channel to leave.
     * */
    fun leaveGroup(channel: Channel) {
        dismissChannelAction()

        chatDomain.leaveChannel(channel.id).enqueue()
    }

    /**
     * Dismisses the [activeChannelAction] and removes it from the UI.
     * */
    fun dismissChannelAction() {
        activeChannelAction = null
    }
}
