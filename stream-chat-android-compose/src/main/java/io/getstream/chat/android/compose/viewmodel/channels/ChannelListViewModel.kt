package io.getstream.chat.android.compose.viewmodel.channels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.call.toUnitCall
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.state.channels.list.Cancel
import io.getstream.chat.android.compose.state.channels.list.ChannelAction
import io.getstream.chat.android.compose.state.channels.list.ChannelItemState
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.experimental.extensions.asReferenced
import io.getstream.chat.android.offline.experimental.querychannels.state.ChannelsStateData
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import io.getstream.chat.android.offline.model.ConnectionState
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import kotlinx.coroutines.flow.Flow
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
 * @param chatClient Used to connect to the API.
 * @param chatDomain Used to connect to the API and fetch the domain status.
 * @param initialSort The initial sort used for [Channel]s.
 * @param initialFilters The current data filter. Users can change this state using [setFilters] to
 * impact which data is shown on the UI.
 * @param channelLimit How many channels we fetch per page.
 * @param memberLimit How many members are fetched for each channel item when loading channels.
 * @param messageLimit How many messages are fetched for each channel item when loading channels.
 */
public class ChannelListViewModel(
    public val chatClient: ChatClient,
    public val chatDomain: ChatDomain,
    initialSort: QuerySort<Channel>,
    private val initialFilters: FilterObject,
    private val channelLimit: Int = DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
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
    public var activeChannelAction: ChannelAction? by mutableStateOf(null)
        private set

    /**
     * The state of our network connection - if we're online or not.
     */
    @Deprecated("Use connectionState instead")
    public val isOnline: Flow<Boolean> = chatDomain.connectionState.map { it == ConnectionState.CONNECTED }

    /**
     * The state of our network connection - if we're online, connecting or offline.
     */
    public val connectionState: StateFlow<ConnectionState> = chatDomain.connectionState

    /**
     * The state of the currently logged in user.
     */
    public val user: StateFlow<User?> = chatDomain.user

    /**
     * Gives us the information about the list of channels mutes by the current user.
     */
    public val channelMutes: StateFlow<List<ChannelMute>> = chatDomain.channelMutes

    /**
     * Checks if the channel is muted for the current user.
     *
     * @param cid The CID of the channel that needs to be checked.
     * @return True if the channel is muted for the current user.
     */
    public fun isChannelMuted(cid: String): Boolean {
        return channelMutes.value.any { cid == it.channel.cid }
    }

    /**
     * Current query channels state that contains filter, sort and other states related to channels query.
     */
    private var queryChannelsState: QueryChannelsState? = null

    /**
     * Combines the latest search query and filter to fetch channels and emit them to the UI.
     */
    init {
        viewModelScope.launch {
            if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_OFFLINE)) {
                initWithOfflinePlugin()
            } else {
                initWithChatDomain()
            }
        }
    }

    /**
     * Initializes this ViewModel with ChatDomain implementation. It makes the initial query to request channels
     * and starts to observe state changes.
     *
     * Note: This method can be removed once OfflinePlugin is completed and released.
     */
    private suspend fun initWithChatDomain() {
        searchQuery.combine(queryConfig) { query, config -> query to config }
            .collectLatest { (query, config) ->
                val result = chatDomain.queryChannels(
                    filter = createQueryChannelsFilter(config.filters, query),
                    sort = config.querySort,
                    messageLimit = messageLimit,
                    limit = channelLimit,
                    memberLimit = memberLimit
                ).await()

                if (result.isSuccess) {
                    observeChannels(controller = result.data(), searchQuery = query)
                } else {
                    result.error().cause?.printStackTrace()
                    channelsState = channelsState.copy(isLoading = false, channelItems = emptyList())
                }
            }
    }

    /**
     * Initializes this ViewModel with OfflinePlugin implementation. It makes the initial query to request channels
     * and starts to observe state changes.
     */
    private suspend fun initWithOfflinePlugin() {
        searchQuery.combine(queryConfig) { query, config -> query to config }
            .collectLatest { (query, config) ->
                val queryChannelsRequest = QueryChannelsRequest(
                    filter = createQueryChannelsFilter(config.filters, query),
                    querySort = config.querySort,
                    limit = channelLimit,
                    messageLimit = messageLimit,
                    memberLimit = memberLimit,
                )
                queryChannelsState =
                    chatClient.asReferenced().queryChannels(queryChannelsRequest).asState(viewModelScope)
                queryChannelsState?.let {
                    observeChannels(it, searchQuery = query)
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
     *
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
     *
     * @param controller The controller used to query channels and their states.
     * @param searchQuery The search query string used to search channels.
     */
    private suspend fun observeChannels(controller: QueryChannelsController, searchQuery: String) {
        chatDomain.channelMutes.combine(controller.channelsState, ::Pair)
            .map { (channelMutes, state) ->
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
                            channelItems = emptyList(),
                            searchQuery = searchQuery
                        )
                    }
                    is QueryChannelsController.ChannelsState.Result -> {
                        channelsState.copy(
                            isLoading = false,
                            channelItems = createChannelItems(state.channels, channelMutes),
                            isLoadingMore = false,
                            endOfChannels = controller.endOfChannels.value,
                            searchQuery = searchQuery
                        )
                    }
                }
            }.collectLatest { newState -> channelsState = newState }
    }

    /**
     * Kicks off operations required to combine and build the [ChannelsState] object for the UI.
     *
     * It connects the 'loadingMore', 'channelsState' and 'endOfChannels' properties from the [queryChannelsState].
     * @param queryChannelsState The state that contains information about query channels.
     * @param searchQuery The search query string used to search channels.
     */
    private suspend fun observeChannels(queryChannelsState: QueryChannelsState, searchQuery: String) {
        chatDomain.channelMutes.combine(queryChannelsState.channelsStateData, ::Pair)
            .map { (channelMutes, state) ->
                when (state) {
                    ChannelsStateData.NoQueryActive,
                    ChannelsStateData.Loading,
                    -> channelsState.copy(
                        isLoading = true,
                        searchQuery = searchQuery
                    )
                    ChannelsStateData.OfflineNoResults -> {
                        channelsState.copy(
                            isLoading = false,
                            channelItems = emptyList(),
                            searchQuery = searchQuery
                        )
                    }
                    is ChannelsStateData.Result -> {
                        channelsState.copy(
                            isLoading = false,
                            channelItems = createChannelItems(state.channels, channelMutes),
                            isLoadingMore = false,
                            endOfChannels = queryChannelsState.endOfChannels.value,
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
        val call = if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_OFFLINE)) {
            val currentQuery = queryChannelsState?.nextPageRequest?.value
            currentQuery?.copy(
                filter = createQueryChannelsFilter(currentConfig.filters, searchQuery.value),
                querySort = currentConfig.querySort
            )?.let {
                chatClient.queryChannels(it)
            }
        } else {
            chatDomain.queryChannelsLoadMore(filter, currentConfig.querySort)
        }

        call?.enqueue()
    }

    /**
     * Clears the active action if we've chosen [Cancel], otherwise, stores the selected action as
     * the currently active action, in [activeChannelAction].
     *
     * It also removes the [selectedChannel] if the action is [Cancel].
     *
     * @param channelAction The selected action.
     */
    public fun performChannelAction(channelAction: ChannelAction) {
        if (channelAction is Cancel) {
            selectedChannel.value = null
        }

        activeChannelAction = if (channelAction == Cancel) {
            null
        } else {
            channelAction
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
     * Deletes a channel, after the user chooses the delete [ChannelAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel The channel to delete.
     */
    public fun deleteConversation(channel: Channel) {
        dismissChannelAction()

        chatClient.channel(channel.cid).delete().toUnitCall().enqueue()
    }

    /**
     * Leaves a channel, after the user chooses the leave [ChannelAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel The channel to leave.
     */
    public fun leaveGroup(channel: Channel) {
        dismissChannelAction()

        chatClient.getCurrentUser()?.let { user ->
            chatClient.removeMembers(channel.type, channel.id, listOf(user.id)).enqueue()
        }
    }

    /**
     * Dismisses the [activeChannelAction] and removes it from the UI.
     */
    public fun dismissChannelAction() {
        activeChannelAction = null
        selectedChannel.value = null
    }

    /**
     * Creates a list of [ChannelItemState] that represents channel items we show in the list of channels.
     *
     * @param channels The channels to show.
     * @param channelMutes The list of channels muted for the current user.
     *
     */
    private fun createChannelItems(channels: List<Channel>, channelMutes: List<ChannelMute>): List<ChannelItemState> {
        val mutedChannelIds = channelMutes.map { channelMute -> channelMute.channel.cid }.toSet()
        return channels.map { ChannelItemState(it, it.cid in mutedChannelIds) }
    }

    internal companion object {
        /**
         * Default value of number of channels to return when querying channels.
         */
        internal const val DEFAULT_CHANNEL_LIMIT = 30

        /**
         * Default value of the number of messages to include in each channel when querying channels.
         */
        internal const val DEFAULT_MESSAGE_LIMIT = 1

        /**
         * Default value of the number of members to include in each channel when querying channels.
         */
        internal const val DEFAULT_MEMBER_LIMIT = 30
    }
}
