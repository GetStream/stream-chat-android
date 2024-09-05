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

package io.getstream.chat.android.compose.viewmodel.channels

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.core.utils.Debouncer
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.extensions.queryChannelsAsState
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.ui.common.state.channels.actions.Cancel
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.uiutils.extension.defaultChannelListFilter
import io.getstream.log.taggedLogger
import io.getstream.result.call.toUnitCall
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/**
 * A state store that represents all the information required to query, filter, show and react to
 * [Channel] items in a list.
 *
 * @param chatClient Used to connect to the API.
 * @param initialSort The initial sort used for [Channel]s.
 * @param initialFilters The current data filter. Users can change this state using [setFilters] to
 * impact which data is shown on the UI.
 * @param channelLimit How many channels we fetch per page.
 * @param memberLimit How many members are fetched for each channel item when loading channels.
 * @param messageLimit How many messages are fetched for each channel item when loading channels.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] used to create [ChatEventHandler].
 * @param searchDebounceMs The debounce time for search queries.
 */
@Suppress("TooManyFunctions")
public class ChannelListViewModel(
    public val chatClient: ChatClient,
    initialSort: QuerySorter<Channel>,
    initialFilters: FilterObject?,
    private val channelLimit: Int = DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
    searchDebounceMs: Long = SEARCH_DEBOUNCE_MS,
) : ViewModel() {

    private val logger by taggedLogger("Chat:ChannelListVM")

    /**
     * The scope used for channel list operations.
     */
    private val chListScope = viewModelScope.let { it + SupervisorJob(it.coroutineContext.job) }

    /**
     * The scope used for search operations.
     */
    private val searchScope = viewModelScope.let { it + SupervisorJob(it.coroutineContext.job) }

    /**
     * The debouncer used for search operations.
     */
    private val searchDebouncer = Debouncer(searchDebounceMs, searchScope)

    /**
     * State flow that keeps the value of the current [FilterObject] for channels.
     */
    private val filterFlow: MutableStateFlow<FilterObject?> = MutableStateFlow(initialFilters)

    /**
     * State flow that keeps the value of the current [QuerySorter] for channels.
     */
    private val querySortFlow: MutableStateFlow<QuerySorter<Channel>> = MutableStateFlow(initialSort)

    /**
     * The currently active query configuration, stored in a [MutableStateFlow]. It's created using
     * the `initialFilters` parameter and the initial sort, but can be changed.
     */
    private val queryConfigFlow = filterFlow.filterNotNull().combine(querySortFlow) { filters, sort ->
        QueryConfig(filters = filters, querySort = sort)
    }

    /**
     * The current state of the search input. When changed, it emits a new value in a flow, which
     * queries and loads new data.
     */
    private val searchQuery = MutableStateFlow<SearchQuery>(SearchQuery.Empty)

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
     * The state of our network connection - if we're online, connecting or offline.
     */
    public val connectionState: StateFlow<ConnectionState> = chatClient.clientState.connectionState

    /**
     * The state of the currently logged in user.
     */
    public val user: StateFlow<User?> = chatClient.clientState.user

    /**
     * Gives us the information about the list of channels mutes by the current user.
     */
    public val channelMutes: StateFlow<List<ChannelMute>> = chatClient.globalState.channelMutes

    /**
     * Builds the default channel filter, which represents "messaging" channels that the current user is a part of.
     */
    private fun buildDefaultFilter(): Flow<FilterObject> {
        return chatClient.clientState.user.map(Filters::defaultChannelListFilter).filterNotNull()
    }

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
    private var queryChannelsState: StateFlow<QueryChannelsState?> = MutableStateFlow(null)

    /**
     * The current state of the search Messages. When changed, it emits a new value in a flow, which
     * queries and loads new data.
     */
    private val searchMessageState: MutableStateFlow<SearchMessageState?> = MutableStateFlow(null)

    private var lastNextQuery: QueryChannelsRequest? = null

    /**
     * Combines the latest search query and filter to fetch channels and emit them to the UI.
     */
    init {
        if (initialFilters == null) {
            viewModelScope.launch {
                val filter = buildDefaultFilter().first()

                this@ChannelListViewModel.filterFlow.value = filter
            }
        }

        viewModelScope.launch {
            init()
        }
    }

    /**
     * Makes the initial query to request channels and starts observing state changes.
     */
    private suspend fun init() {
        logger.d { "[init] no args" }
        searchQuery.combine(queryConfigFlow) { query, config -> query to config }
            .collectLatest { (query, config) ->
                when (query) {
                    is SearchQuery.Empty,
                    is SearchQuery.Channels,
                    -> {
                        searchScope.coroutineContext.cancelChildren()
                        observeQueryChannels(query.getConfig(config))
                    }
                    is SearchQuery.Messages -> {
                        chListScope.coroutineContext.cancelChildren()
                        handleSearchQuery(query.query)
                        observeSearchMessages(query.query)
                    }
                }
            }
    }

    private fun SearchQuery.getConfig(defaultConfig: QueryConfig<Channel>): QueryConfig<Channel> {
        return when (this) {
            is SearchQuery.Channels -> defaultConfig.copy(
                filters = createQueryChannelsFilter(defaultConfig.filters, query),
            )
            is SearchQuery.Messages -> defaultConfig
            is SearchQuery.Empty -> defaultConfig
        }
    }

    private suspend fun observeSearchMessages(query: String) = runCatching {
        logger.d { "[observeSearchMessages] query: '$query'" }
        searchMessageState.filterNotNull().collectLatest {
            logger.v { "[observeSearchMessages] state: ${it.stringify()}" }
            channelsState = channelsState.copy(
                searchQuery = searchQuery.value,
                isLoading = it.isLoading,
                isLoadingMore = it.isLoadingMore,
                endOfChannels = !it.canLoadMore,
                channelItems = it.messages.map(ItemState::SearchResultItemState),
            )
        }
    }.onFailure {
        when (it is CancellationException) {
            true -> logger.v { "[observeSearchMessages] cancelled('$query')" }
            else -> logger.e { "[observeSearchMessages] failed: $it" }
        }
    }

    private fun handleSearchQuery(query: String) {
        logger.d { "[handleSearchQuery] query: '$query'" }
        searchDebouncer.submitSuspendable {
            searchMessagesForQuery(query)
        }
    }

    private suspend fun searchMessagesForQuery(query: String) {
        logger.d { "[searchMessagesForQuery] query: '$query'" }
        val channelFilter = filterFlow.value ?: Filters.defaultChannelListFilter(user.value) ?: run {
            logger.v { "[searchMessagesForQuery] rejected (no channel filter)" }
            return
        }
        val newState = SearchMessageState(query = query, isLoading = true)
        searchMessageState.value = newState
        searchMessageState.value = searchMessages(src = "new", newState, channelFilter).also {
            logger.v { "[searchMessagesForQuery] completed('$query'): ${it.messages.size}" }
        }
    }

    private suspend fun loadMoreQueryMessages() {
        logger.d { "[loadMoreQueryMessages] no args" }
        val channelFilter = filterFlow.value ?: Filters.defaultChannelListFilter(user.value) ?: run {
            logger.v { "[loadMoreQueryMessages] rejected (no channel filter)" }
            return
        }
        val currentState = searchMessageState.value ?: run {
            logger.v { "[loadMoreQueryMessages] rejected (no current state)" }
            return
        }
        if (currentState.isLoading) {
            logger.v { "[loadMoreQueryMessages] rejected (already loading)" }
            return
        }
        if (currentState.isLoadingMore) {
            logger.v { "[loadMoreQueryMessages] rejected (already loading more)" }
            return
        }
        if (!currentState.canLoadMore) {
            logger.v { "[loadMoreQueryMessages] rejected (end of messages)" }
            return
        }
        val query = currentState.query
        logger.v { "[loadMoreQueryMessages] query: 'query'" }
        val newState = currentState.copy(isLoadingMore = true)
        searchMessageState.value = newState
        searchMessageState.value = searchMessages(src = "more", newState, channelFilter).also {
            logger.v { "[loadMoreQueryMessages] completed('$query'): ${it.messages.size}" }
        }
    }

    /**
     * Searches for messages based on the current query.
     */
    private suspend fun searchMessages(
        src: String,
        currentState: SearchMessageState,
        channelFilter: FilterObject,
    ): SearchMessageState {
        val offset = currentState.messages.size
        val limit = channelLimit
        logger.v { "[searchMessages] #$src; query: '${currentState.query}', offset: $offset, limit: $limit" }
        val result = chatClient.searchMessages(
            channelFilter = channelFilter,
            messageFilter = Filters.autocomplete("text", currentState.query),
            offset = offset,
            limit = limit,
        ).await()
        return when (result) {
            is io.getstream.result.Result.Success -> {
                logger.v { "[searchMessages] #$src; completed(messages.size: ${result.value.messages.size})" }
                currentState.copy(
                    messages = currentState.messages + result.value.messages,
                    isLoading = false,
                    isLoadingMore = false,
                    canLoadMore = result.value.messages.size >= limit,
                )
            }
            is io.getstream.result.Result.Failure -> {
                logger.e { "[searchMessages] #$src; failed: ${result.value}" }
                currentState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    canLoadMore = true,
                )
            }
        }
    }

    private suspend fun observeQueryChannels(config: QueryConfig<Channel>) = runCatching {
        val queryChannelsRequest = QueryChannelsRequest(
            filter = config.filters,
            querySort = config.querySort,
            limit = channelLimit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
        )
        logger.d { "[observeQueryChannels] request: $queryChannelsRequest" }
        queryChannelsState = chatClient.queryChannelsAsState(
            request = queryChannelsRequest,
            chatEventHandlerFactory = chatEventHandlerFactory,
            coroutineScope = chListScope,
        )
        queryChannelsState.filterNotNull().collectLatest { queryChannelsState ->
            combine(
                queryChannelsState.channelsStateData,
                channelMutes,
                chatClient.globalState.typingChannels,
            ) { state, channelMutes, typingChannels ->
                when (state) {
                    ChannelsStateData.NoQueryActive,
                    ChannelsStateData.Loading,
                    -> channelsState.copy(
                        isLoading = true,
                        searchQuery = searchQuery.value,
                    ).also { logger.d { "[observeQueryChannels] state: Loading" } }
                    ChannelsStateData.OfflineNoResults -> {
                        logger.v { "[observeQueryChannels] state: OfflineNoResults(channels are empty)" }
                        channelsState.copy(
                            isLoading = false,
                            channelItems = emptyList(),
                            searchQuery = searchQuery.value,
                        )
                    }
                    is ChannelsStateData.Result -> {
                        logger.v { "[observeQueryChannels] state: Result(channels.size: ${state.channels.size})" }
                        channelsState.copy(
                            isLoading = false,
                            channelItems = createChannelItems(
                                channels = state.channels,
                                channelMutes = channelMutes,
                                typingEvents = typingChannels,
                            ),
                            isLoadingMore = false,
                            endOfChannels = queryChannelsState.endOfChannels.value,
                            searchQuery = searchQuery.value,
                        )
                    }
                }
            }.collectLatest { newState -> channelsState = newState }
        }
    }.onFailure {
        when (it is CancellationException) {
            true -> logger.v { "[observeQueryChannels] cancelled" }
            else -> logger.e { "[observeQueryChannels] failed: $it" }
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
                        Filters.notExists("name"),
                    ),
                    Filters.autocomplete("name", searchQuery),
                ),
            )
        } else {
            filter
        }
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
    @Deprecated(
        message = "Use setSearchQuery instead",
        replaceWith = ReplaceWith(
            expression = "setSearchQuery(SearchQuery.Channels(newQuery))",
            imports = ["io.getstream.chat.android.compose.state.channels.list.SearchQuery"],
        ),
    )
    public fun setSearchQuery(newQuery: String) {
        this.searchQuery.value = SearchQuery.Messages(newQuery)
    }

    public fun setSearchQuery(searchQuery: SearchQuery) {
        logger.d { "[setSearchQuery] searchQuery: $searchQuery" }
        this.searchQuery.value = searchQuery
    }

    /**
     * Allows for the change of filters used for channel queries.
     *
     * Use this if you need to support runtime filter changes, through custom filters UI.
     *
     * Warning: The filter that's applied will override the `initialFilters` set through the constructor.
     *
     * @param newFilters The new filters to be used as a baseline for filtering channels.
     */
    public fun setFilters(newFilters: FilterObject) {
        this.filterFlow.tryEmit(value = newFilters)
    }

    /**
     * Allows for the change of the query sort used for channel queries.
     *
     * Use this if you need to support runtime sort changes, through custom sort UI.
     */
    public fun setQuerySort(querySort: QuerySorter<Channel>) {
        this.querySortFlow.tryEmit(value = querySort)
    }

    /**
     * Loads more data when the user reaches the end of the channels list.
     */
    public fun loadMore() {
        logger.d { "[loadMore] no args" }

        if (chatClient.clientState.isOffline) {
            logger.v { "[loadMore] rejected (client is offline)" }
            return
        }
        when (searchQuery.value) {
            is SearchQuery.Empty,
            is SearchQuery.Channels,
            -> chListScope.launch { loadMoreQueryChannels() }
            is SearchQuery.Messages,
            -> searchScope.launch { loadMoreQueryMessages() }
        }
    }

    private suspend fun loadMoreQueryChannels() {
        logger.d { "[loadMoreQueryChannels] no args" }
        val currentFilter = filterFlow.value
        if (currentFilter == null) {
            logger.v { "[loadMoreQueryChannels] rejected (no current filter)" }
            return
        }
        val currentQuery = queryChannelsState.value?.nextPageRequest?.value
        if (currentQuery == null) {
            logger.v { "[loadMoreQueryChannels] rejected (no current query)" }
            return
        }
        if (channelsState.endOfChannels) {
            logger.v { "[loadMoreQueryChannels] rejected (end of channels)" }
            return
        }
        if (channelsState.isLoadingMore) {
            logger.v { "[loadMoreQueryChannels] rejected (already loading more)" }
            return
        }
        val nextQuery = currentQuery.copy(
            filter = createQueryChannelsFilter(currentFilter, searchQuery.value.query),
            querySort = querySortFlow.value,
        )
        if (lastNextQuery == nextQuery) {
            logger.v { "[loadMoreQueryChannels] rejected (same query)" }
            return
        }
        lastNextQuery = nextQuery
        logger.v { "[loadMoreQueryChannels] offset: ${nextQuery.offset}, limit: ${nextQuery.limit}" }
        channelsState = channelsState.copy(isLoadingMore = true)
        val result = chatClient.queryChannels(nextQuery).await()
        if (result.isSuccess) {
            logger.v { "[loadMoreQueryChannels] completed; channels.size: ${result.getOrNull()?.size}" }
        } else {
            logger.e { "[loadMoreQueryChannels] failed: ${result.errorOrNull()}" }
        }
        channelsState = channelsState.copy(isLoadingMore = false)
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

        chatClient.clientState.user.value?.let { user ->
            chatClient.channel(channel.type, channel.id).removeMembers(listOf(user.id)).enqueue()
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
    private fun createChannelItems(
        channels: List<Channel>,
        channelMutes: List<ChannelMute>,
        typingEvents: Map<String, TypingEvent>,
    ): List<ItemState.ChannelItemState> {
        val mutedChannelIds = channelMutes.map { channelMute -> channelMute.channel.cid }.toSet()
        return channels.map {
            ItemState.ChannelItemState(
                channel = it,
                isMuted = it.cid in mutedChannelIds,
                typingUsers = typingEvents[it.cid]?.users ?: emptyList(),
            )
        }
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

        /**
         * Debounce time for search queries.
         */
        private const val SEARCH_DEBOUNCE_MS = 200L
    }

    private data class SearchMessageState(
        val query: String = "",
        val canLoadMore: Boolean = true,
        val messages: List<Message> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingMore: Boolean = false,
    ) {

        fun stringify(): String {
            return "SearchMessageState(" +
                "query='$query', " +
                "messages.size=${messages.size}, " +
                "isLoading=$isLoading, " +
                "isLoadingMore=$isLoadingMore, " +
                "canLoadMore=$canLoadMore)"
        }
    }
}
