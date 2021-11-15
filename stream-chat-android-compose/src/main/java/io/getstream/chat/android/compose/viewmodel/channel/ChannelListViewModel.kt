package io.getstream.chat.android.compose.viewmodel.channel

import androidx.compose.runtime.MutableState
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
import io.getstream.chat.android.compose.ui.util.isMuted
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * A state store that represents all the information required to query, filter, show and react to
 * [Channel] items in a list.
 *
 * @param initialSort The initial sort used for [Channel]s.
 * @param initialFilters The current data filter. Users can change this state using [setFilters] to
 * impact which data is shown on the UI.
 */
public class ChannelListViewModel(
    public val chatClient: ChatClient,
    public val chatDomain: ChatDomain,
    initialSort: QuerySort<Channel>,
    private val initialFilters: FilterObject,
) : ViewModel() {

    /**
     * The currently active query configuration, stored in a [MutableStateFlow]. It's created using
     * the [initialFilters] and the initial sort, but can be changed.
     */
    private val queryConfig = MutableStateFlow(QueryConfig(initialFilters, initialSort))

    /**
     * The current state of the search input. When changed, it emits a new value in a flow, which
     * queries for a new [QueryChannelsController] and loads new data.
     */
    private val searchQuery = MutableStateFlow("")

    /**
     * The current state of the channels screen. It holds all the information required to render the UI.
     */
    public var channelsState: ChannelsState by mutableStateOf(ChannelsState())
        private set

    /**
     * Currently selected channel, if any. Used to show the bottom drawer information when long
     * tapping on a list item.
     */
    public var selectedChannel: MutableState<Channel?> = mutableStateOf(null)
        private set

    /**
     * Currently active channel action, if any. Used to show a dialog for deleting or leaving a
     * channel/conversation.
     */
    public var activeChannelAction: ChannelListAction? by mutableStateOf(null)
        private set

    /**
     * The state of our network connection - if we're online or not.
     */
    @Deprecated("Use connectionState instead")
    public val isOnline: StateFlow<Boolean> = chatDomain.online

    /**
     * The state of our network connection - if we're online, connecting or offline.
     */
    public val connectionState: StateFlow<ConnectionState> = chatDomain.connectionState

    /**
     * The state of the currently logged in user.
     */
    public val user: StateFlow<User?> = chatDomain.user

    /**
     * Combines the latest search query and filter to fetch channels and emit them to the UI.
     */
    init {
        viewModelScope.launch {
            searchQuery.combine(queryConfig) { query, config -> query to config }
                .collectLatest { (query, config) ->
                    val result = chatDomain.queryChannels(
                        filter = createQueryChannelsFilter(config.filters, query),
                        sort = config.querySort
                    ).await()

                    if (result.isSuccess) {
                        observeChannels(controller = result.data(), searchQuery = query)
                    } else {
                        result.error().cause?.printStackTrace()
                        channelsState = channelsState.copy(isLoading = false, channels = emptyList())
                    }
                }
        }
    }

    /**
     * Creates a filter that is used to query channels.
     *
     * If the [searchQuery] is empty, then returns the original [filter] provided by the user.
     * Otherwise, returns a wrapped [filter] that also checks that the channel name match the
     * [searchQuery].
     *
     * @param filter The filter that was passed by the user.
     * @param searchQuery The search query used to filter the channels.
     * @return The filter that will be used to query channels.
     */
    private fun createQueryChannelsFilter(filter: FilterObject, searchQuery: String): FilterObject {
        return if (searchQuery.isNotEmpty()) {
            Filters.and(
                filter,
                Filters.or(
                    Filters.and(
                        Filters.autocomplete("member.user.name", searchQuery),
                        Filters.notExists("name")
                    ),
                    Filters.autocomplete("name", searchQuery)
                )
            )
        } else {
            filter
        }
    }

    /**
     * Kicks off operations required to combine and build the [ChannelsState] object for the UI.
     *
     * It connects the 'loadingMore', 'channelsState' and 'endOfChannels' properties from the [controller].
     */
    private suspend fun observeChannels(controller: QueryChannelsController, searchQuery: String) {
        controller.mutedChannelIds.combine(controller.channelsState, ::Pair)
            .map { (mutedChannelIds, state) ->
                when (state) {
                    QueryChannelsController.ChannelsState.NoQueryActive,
                    QueryChannelsController.ChannelsState.Loading,
                    -> channelsState.copy(
                        isLoading = true,
                        searchQuery = searchQuery
                    )
                    QueryChannelsController.ChannelsState.OfflineNoResults -> {
                        channelsState.copy(
                            isLoading = false,
                            channels = emptyList(),
                            searchQuery = searchQuery
                        )
                    }
                    is QueryChannelsController.ChannelsState.Result -> {
                        channelsState.copy(
                            isLoading = false,
                            channels = enrichMutedChannels(state.channels, mutedChannelIds),
                            isLoadingMore = false,
                            endOfChannels = controller.endOfChannels.value,
                            searchQuery = searchQuery
                        )
                    }
                }
            }.collectLatest { newState -> channelsState = newState }
    }

    /**
     * Changes the currently selected channel state. This updates the UI state and allows us to observe
     * the state change.
     */
    public fun selectChannel(channel: Channel?) {
        this.selectedChannel.value = channel
    }

    /**
     * Changes the current query state. This updates the data flow and triggers another query operation.
     *
     * The new operation will hold the channels that match the new query.
     */
    public fun setSearchQuery(newQuery: String) {
        this.searchQuery.value = newQuery
    }

    /**
     * Allows for the change of filters used for channel queries.
     *
     * Use this if you need to support runtime filter changes, through custom filters UI.
     */
    public fun setFilters(newFilters: FilterObject) {
        val currentConfig = this.queryConfig.value

        this.queryConfig.value =
            currentConfig.copy(filters = Filters.and(initialFilters, newFilters))
    }

    /**
     * Allows for the change of the query sort used for channel queries.
     *
     * Use this if you need to support runtime sort changes, through custom sort UI.
     */
    public fun setQuerySort(querySort: QuerySort<Channel>) {
        val currentConfig = this.queryConfig.value

        this.queryConfig.value =
            currentConfig.copy(querySort = querySort)
    }

    /**
     * Loads more data when the user reaches the end of the channels list.
     */
    public fun loadMore() {
        val currentConfig = queryConfig.value
        val query = searchQuery.value

        val filter = if (query.isNotEmpty()) {
            Filters.and(currentConfig.filters, Filters.autocomplete("name", query))
        } else {
            currentConfig.filters
        }

        channelsState = channelsState.copy(isLoadingMore = true)
        chatDomain.queryChannelsLoadMore(filter, currentConfig.querySort).enqueue()
    }

    /**
     * Clears the active action if we've chosen [Cancel], otherwise, stores the selected action as
     * the currently active action, in [activeChannelAction].
     *
     * It also removes the [selectedChannel] if the action is [Cancel].
     *
     * @param channelListAction The selected action.
     */
    public fun performChannelAction(channelListAction: ChannelListAction) {
        if (channelListAction is Cancel) {
            selectedChannel.value = null
        }

        activeChannelAction = if (channelListAction == Cancel) {
            null
        } else {
            channelListAction
        }
    }

    /**
     * Mutes a channel.
     *
     * @param channel The channel to mute.
     */
    public fun muteChannel(channel: Channel) {
        dismissChannelAction()

        chatClient.muteChannel(channel.type, channel.id).enqueue()
    }

    /**
     * Unmutes a channel.
     *
     * @param channel The channel to unmute.
     */
    public fun unmuteChannel(channel: Channel) {
        dismissChannelAction()

        chatClient.unmuteChannel(channel.type, channel.id).enqueue()
    }

    /**
     * Deletes a channel, after the user chooses the delete [ChannelListAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel The channel to delete.
     */
    public fun deleteConversation(channel: Channel) {
        dismissChannelAction()

        chatDomain.deleteChannel(channel.id).enqueue()
    }

    /**
     * Leaves a channel, after the user chooses the leave [ChannelListAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel The channel to leave.
     */
    public fun leaveGroup(channel: Channel) {
        dismissChannelAction()

        chatDomain.leaveChannel(channel.cid).enqueue()
    }

    /**
     * Dismisses the [activeChannelAction] and removes it from the UI.
     */
    public fun dismissChannelAction() {
        activeChannelAction = null
        selectedChannel.value = null
    }

    /**
     * Uses [Channel.isMuted] property to store additional flag for each channel if the channel is muted
     * for the current user.
     *
     * @param channels The list of channels channels without the information about channel mutes.
     * @param mutedChannelIds The list of channel IDs that represent channels muted for the current user.
     * @return The list of channels enriched with the information about channel mutes.
     *
     * @see [Channel.isMuted]
     */
    private fun enrichMutedChannels(channels: List<Channel>, mutedChannelIds: List<String>): List<Channel> {
        val mutedChannelIdsSet = mutedChannelIds.toSet()
        return channels.map { channel ->
            val isMuted = channel.id in mutedChannelIdsSet

            if (channel.isMuted != isMuted) {
                channel.copy(extraData = channel.extraData.toMutableMap())
                    .also { it.isMuted = isMuted }
            } else {
                channel
            }
        }
    }
}
