/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.client.api.event.ChatEventHandler
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.ChannelsStateData
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.api.state.globalStateFlow
import io.getstream.chat.android.client.api.state.initGroupedQueryChannelsAsState
import io.getstream.chat.android.client.api.state.queryChannelsAsState
import io.getstream.chat.android.client.internal.state.event.handler.grouped.internal.groupAwareChatEventHandlerFactory
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.state.channels.list.ChannelListAction
import io.getstream.chat.android.compose.state.channels.list.ChannelListEvent
import io.getstream.chat.android.compose.state.channels.list.ChannelsState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.util.extensions.asState
import io.getstream.chat.android.core.utils.Debouncer
import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.utils.extensions.defaultChannelListFilter
import io.getstream.chat.android.ui.common.utils.extensions.isOneToOne
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.toUnitCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel managing the state of a given Channel List.
 *
 * @param chatClient The prepared [ChatClient] instance required for fetching the data.
 * @param channelLimit How many channels we fetch per page.
 * @param memberLimit How many members are fetched for each channel item when loading channels.
 * When `null`, the server-side default is used.
 * @param messageLimit How many messages are fetched for each channel item when loading channels.
 * When `null`, the server-side default is used.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] used to create [ChatEventHandler].
 * @param searchDebounceMs The debounce time for search queries.
 * @param draftMessagesEnabled If the draft message feature is enabled.
 * @param messageSearchSort Sorting for message search results. When `null`, the server-side default is used.
 * @param globalState A flow emitting the current [GlobalState].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Suppress("TooManyFunctions", "LongParameterList", "LargeClass")
public class ChannelListViewModel internal constructor(
    public val chatClient: ChatClient,
    private val mode: QueryMode,
    private val channelLimit: Int,
    private val memberLimit: Int?,
    private val messageLimit: Int?,
    private val chatEventHandlerFactory: ChatEventHandlerFactory,
    searchDebounceMs: Long,
    private val draftMessagesEnabled: Boolean,
    private val messageSearchSort: QuerySorter<Message>?,
    private val globalState: Flow<GlobalState>,
) : ViewModel() {

    /** Internal discriminator for the query modes supported by this ViewModel. */
    internal sealed interface QueryMode {
        data class Standard(
            val initialFilter: FilterObject?,
            val initialSort: QuerySorter<Channel>,
        ) : QueryMode

        data class Predefined(
            val name: String,
            val filterValues: Map<String, Any>?,
            val sortValues: Map<String, Any>?,
        ) : QueryMode

        data class Grouped(val groupKey: String) : QueryMode
    }

    /**
     * Creates a view model that queries channels by an explicit filter and sort.
     *
     * @param chatClient The prepared [ChatClient] instance required for fetching the data.
     * @param initialSort The initial sort used for [Channel]s. Can be changed at runtime via [setQuerySort].
     * @param initialFilters The data filter. When `null`, a default filter scoped to "messaging" channels the current
     * user is a member of is used. Can be changed at runtime via [setFilters].
     * @param channelLimit How many channels we fetch per page.
     * @param memberLimit How many members are fetched for each channel item when loading channels.
     * When `null`, the server-side default is used.
     * @param messageLimit How many messages are fetched for each channel item when loading channels.
     * When `null`, the server-side default is used.
     * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] used to create [ChatEventHandler].
     * @param searchDebounceMs The debounce time for search queries.
     * @param isDraftMessageEnabled If the draft message feature is enabled.
     * @param messageSearchSort Sorting for message search results. When `null`, the server-side default is used.
     * @param globalState A flow emitting the current [GlobalState].
     */
    public constructor(
        chatClient: ChatClient,
        initialSort: QuerySorter<Channel> = QuerySortByField.descByName("last_updated"),
        initialFilters: FilterObject? = null,
        channelLimit: Int = DEFAULT_CHANNEL_LIMIT,
        memberLimit: Int? = null,
        messageLimit: Int? = null,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
        searchDebounceMs: Long = SEARCH_DEBOUNCE_MS,
        draftMessagesEnabled: Boolean = true,
        messageSearchSort: QuerySorter<Message>? = null,
        globalState: Flow<GlobalState> = chatClient.globalStateFlow,
    ) : this(
        chatClient = chatClient,
        mode = QueryMode.Standard(initialFilter = initialFilters, initialSort = initialSort),
        channelLimit = channelLimit,
        memberLimit = memberLimit,
        messageLimit = messageLimit,
        chatEventHandlerFactory = chatEventHandlerFactory,
        searchDebounceMs = searchDebounceMs,
        draftMessagesEnabled = draftMessagesEnabled,
        messageSearchSort = messageSearchSort,
        globalState = globalState,
    )

    /**
     * Creates a view model that queries channels using a predefined filter resolved by the server.
     *
     * The filter and sort are identified by [predefinedFilterName] and resolved server-side;
     * [filterValues] and [sortValues] interpolate into the predefined template. [setFilters] and
     * [setQuerySort] do not affect a view model created this way.
     *
     * @param chatClient The prepared [ChatClient] instance required for fetching the data.
     * @param predefinedFilterName The name of the predefined filter registered on the backend.
     * @param filterValues Optional values interpolated into the predefined filter template.
     * @param sortValues Optional values interpolated into the predefined sort template.
     * @param channelLimit How many channels we fetch per page.
     * @param memberLimit How many members are fetched for each channel item when loading channels.
     * When `null`, the server-side default is used.
     * @param messageLimit How many messages are fetched for each channel item when loading channels.
     * When `null`, the server-side default is used.
     * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] used to create [ChatEventHandler].
     * @param searchDebounceMs The debounce time for search queries.
     * @param isDraftMessageEnabled If the draft message feature is enabled.
     * @param messageSearchSort Sorting for message search results. When `null`, the server-side default is used.
     * @param globalState A flow emitting the current [GlobalState].
     */
    public constructor(
        chatClient: ChatClient,
        predefinedFilterName: String,
        filterValues: Map<String, Any>? = null,
        sortValues: Map<String, Any>? = null,
        channelLimit: Int = DEFAULT_CHANNEL_LIMIT,
        memberLimit: Int? = null,
        messageLimit: Int? = null,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
        searchDebounceMs: Long = SEARCH_DEBOUNCE_MS,
        draftMessagesEnabled: Boolean = true,
        messageSearchSort: QuerySorter<Message>? = null,
        globalState: Flow<GlobalState> = chatClient.globalStateFlow,
    ) : this(
        chatClient = chatClient,
        mode = QueryMode.Predefined(
            name = predefinedFilterName,
            filterValues = filterValues,
            sortValues = sortValues,
        ),
        channelLimit = channelLimit,
        memberLimit = memberLimit,
        messageLimit = messageLimit,
        chatEventHandlerFactory = chatEventHandlerFactory,
        searchDebounceMs = searchDebounceMs,
        draftMessagesEnabled = draftMessagesEnabled,
        messageSearchSort = messageSearchSort,
        globalState = globalState,
    )

    /**
     * Grouped channel list constructor. Subscribes to the state identified by [groupKey] without
     * issuing a remote call; the state is populated externally by `queryGroupedChannels` responses.
     *
     * **IMPORTANT: This is an enterprise feature and is disabled by default. For more info, reach out to our
     * Contact & Support.**
     *
     * @param chatClient The prepared [ChatClient] instance required for fetching the data.
     * @param groupKey The name of the channels group.
     * @param searchDebounceMs The debounce time for search queries.
     * @param isDraftMessageEnabled If the draft message feature is enabled.
     * @param messageSearchSort Sorting for message search results. When `null`, the server-side default is used.
     * @param globalState A flow emitting the current [GlobalState].
     */
    public constructor(
        chatClient: ChatClient,
        groupKey: String,
        searchDebounceMs: Long = SEARCH_DEBOUNCE_MS,
        isDraftMessageEnabled: Boolean = false,
        messageSearchSort: QuerySorter<Message>? = null,
        globalState: Flow<GlobalState> = chatClient.globalStateFlow,
    ) : this(
        chatClient = chatClient,
        mode = QueryMode.Grouped(groupKey),
        channelLimit = DEFAULT_CHANNEL_LIMIT,
        memberLimit = null,
        messageLimit = null,
        chatEventHandlerFactory = groupAwareChatEventHandlerFactory(
            groupKey = groupKey,
            clientState = chatClient.clientState,
        ),
        searchDebounceMs = searchDebounceMs,
        draftMessagesEnabled = isDraftMessageEnabled,
        messageSearchSort = messageSearchSort,
        globalState = globalState,
    )

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
     * The debouncer used for channel list operations.
     */
    private val queryChannelDebouncer = Debouncer(searchDebounceMs, chListScope)

    /**
     * Keeps track of the current filter. Only relevant for [QueryMode.Standard], the other modes follow a server-side
     * filtering spec.
     */
    private val filterFlow: MutableStateFlow<FilterObject?> = MutableStateFlow(
        when (mode) {
            is QueryMode.Standard -> mode.initialFilter
            is QueryMode.Predefined -> null
            is QueryMode.Grouped -> null
        },
    )

    /**
     * Keeps track of the current sort spec. Only relevant for [QueryMode.Standard], the other modes follow a
     * server-side sorting spec.
     */
    private val querySortFlow: MutableStateFlow<QuerySorter<Channel>> = MutableStateFlow(
        when (mode) {
            is QueryMode.Standard -> mode.initialSort
            is QueryMode.Predefined -> QuerySortByField()
            is QueryMode.Grouped -> QuerySortByField.descByName("last_updated")
        },
    )

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
    private val _searchQuery = MutableStateFlow<SearchQuery>(SearchQuery.Empty)

    /**
     * The current search query.
     */
    public val searchQuery: SearchQuery by _searchQuery.asState(viewModelScope)

    /**
     * The refresh flow used to trigger a refresh of either channels or search results.
     */
    private val refreshFlow = MutableStateFlow(0L)

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

    // Buffer one emission so action callbacks aren't dropped if no collector is momentarily active.
    private val _events = MutableSharedFlow<ChannelListEvent>(extraBufferCapacity = 1)

    /**
     * Emits [ChannelListEvent]s as channel actions complete. Hot flow with no replay; collect it while the screen is
     * active to surface transient feedback such as a snackbar.
     */
    internal val events: SharedFlow<ChannelListEvent> = _events.asSharedFlow()

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
    public val channelMutes: StateFlow<List<ChannelMute>> = globalState
        .flatMapLatest { it.channelMutes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val globalMuted: StateFlow<List<Mute>> = globalState
        .flatMapLatest { it.muted }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val globalBlockedUserIds: StateFlow<List<String>> = globalState
        .flatMapLatest { it.blockedUserIds }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val typingChannels: StateFlow<Map<String, TypingEvent>> = globalState
        .flatMapLatest { it.typingChannels }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private val channelDraftMessages: StateFlow<Map<String, DraftMessage>> = globalState
        .flatMapLatest { it.channelDraftMessages }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    /**
     * Current query channels state that contains filter, sort and other states related to channels query.
     */
    private var queryChannelsState: StateFlow<QueryChannelsState?> = MutableStateFlow(null)

    /**
     * The current state of the search Messages. When changed, it emits a new value in a flow, which
     * queries and loads new data.
     */
    private val searchMessageState: MutableStateFlow<SearchMessageState?> = MutableStateFlow(null)

    /**
     * Tracks the most recent next-page request issued by [loadMoreQueryChannels] so we can dedupe
     * repeated load-more clicks against an identical paginated request.
     */
    private var lastNextQuery: QueryChannelsRequest? = null

    /**
     * Emits the effective query input to react to. Standard mode reacts to filter/sort changes
     * (via [queryConfigFlow]) in addition to search and refresh; Predefined and Grouped modes
     * have server-owned filter/sort, so they only react to search and refresh.
     */
    private val activeQuery: Flow<SearchQuery> = when (mode) {
        is QueryMode.Standard ->
            combine(_searchQuery, queryConfigFlow, refreshFlow) { query, _, _ -> query }
        is QueryMode.Predefined ->
            combine(_searchQuery, refreshFlow) { query, _ -> query }
        is QueryMode.Grouped ->
            combine(_searchQuery, refreshFlow) { query, _ -> query }
    }

    /**
     * Combines the latest search query and filter to fetch channels and emit them to the UI.
     */
    init {
        if (mode is QueryMode.Standard && mode.initialFilter == null) {
            viewModelScope.launch {
                val filter = defaultChannelsFilter().first()
                this@ChannelListViewModel.filterFlow.value = filter
            }
        }

        viewModelScope.launch {
            init()
        }
    }

    /**
     * Refreshes either channels or search results.
     */
    public fun refresh() {
        logger.d { "[refresh] no args" }
        refreshFlow.value = System.currentTimeMillis()
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
    public fun setSearchQuery(searchQuery: SearchQuery) {
        logger.d { "[setSearchQuery] searchQuery: $searchQuery" }
        // Changing the query starts a fresh load, so any pending load-more error no longer applies.
        channelsState = channelsState.copy(loadingError = false)
        this._searchQuery.value = searchQuery
    }

    /**
     * Allows for the change of filters used for channel queries.
     *
     * Use this if you need to support runtime filter changes, through custom filters UI. The applied
     * filter overrides the `initialFilters` set through the constructor.
     *
     * Warning: The filter that's applied will override the `initialFilters` set through the constructor.
     * Has no effect on view models constructed for a predefined-filter query (predefined identity is
     * fixed at construction) or for a grouped query (the group's filter is server-owned).
     *
     * @param newFilters The new filters to be used as a baseline for filtering channels.
     */
    public fun setFilters(newFilters: FilterObject) {
        when (mode) {
            is QueryMode.Predefined -> {
                logger.w { "[setFilters] ignored — view model uses predefined filter '${mode.name}'" }
                return
            }
            is QueryMode.Grouped -> {
                logger.w { "[setFilters] no-op in Grouped mode (groupKey: ${mode.groupKey})" }
                return
            }
            is QueryMode.Standard -> Unit
        }
        this.filterFlow.tryEmit(value = newFilters)
    }

    /**
     * Allows for the change of the query sort used for channel queries.
     *
     * Use this if you need to support runtime sort changes, through custom sort UI.
     *
     * Has no effect on view models constructed for a predefined-filter query (sort is resolved by
     * the server) or for a grouped query (the group's sort is fixed).
     */
    public fun setQuerySort(querySort: QuerySorter<Channel>) {
        when (mode) {
            is QueryMode.Predefined -> {
                logger.w { "[setQuerySort] ignored — view model uses predefined filter '${mode.name}'" }
                return
            }
            is QueryMode.Grouped -> {
                logger.w { "[setQuerySort] no-op in Grouped mode (groupKey: ${mode.groupKey})" }
                return
            }
            is QueryMode.Standard -> Unit
        }
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
        when (_searchQuery.value) {
            is SearchQuery.Empty,
            is SearchQuery.Channels,
            -> chListScope.launch { loadMoreQueryChannels() }
            is SearchQuery.Messages,
            -> searchScope.launch { loadMoreQueryMessages() }
        }
    }

    /**
     * Executes the given [ChannelAction] immediately if it doesn't require confirmation,
     * or stores it as [activeChannelAction] to show a confirmation dialog.
     *
     * @param action The action to execute or confirm.
     */
    public fun executeOrConfirm(action: ChannelAction) {
        if (action.confirmationPopup != null) {
            activeChannelAction = action
        } else {
            action.onAction()
            dismissChannelAction()
        }
    }

    /**
     * Executes the currently pending [activeChannelAction] after user confirmation
     * and dismisses it from the UI.
     */
    public fun confirmPendingAction() {
        activeChannelAction?.onAction?.invoke()
        dismissChannelAction()
    }

    /**
     * Mutes a channel.
     *
     * @param channel The channel to mute.
     */
    public fun muteChannel(channel: Channel) {
        dismissChannelAction()

        chatClient.muteChannel(channel.type, channel.id)
            .enqueueTrackingError(ChannelListAction.MuteChannel)
    }

    /**
     * Pins a channel.
     *
     * @param channel The channel to pin.
     */
    public fun pinChannel(channel: Channel) {
        dismissChannelAction()
        chatClient.pinChannel(channel.type, channel.id)
            .enqueueTrackingError(ChannelListAction.PinChannel)
    }

    /**
     * Unpins a channel.
     *
     * @param channel The channel to unpin.
     */
    public fun unpinChannel(channel: Channel) {
        dismissChannelAction()
        chatClient.unpinChannel(channel.type, channel.id)
            .enqueueTrackingError(ChannelListAction.UnpinChannel)
    }

    public fun archiveChannel(channel: Channel) {
        dismissChannelAction()
        chatClient.archiveChannel(channel.type, channel.id)
            .enqueueTrackingError(ChannelListAction.ArchiveChannel)
    }

    public fun unarchiveChannel(channel: Channel) {
        dismissChannelAction()
        chatClient.unarchiveChannel(channel.type, channel.id)
            .enqueueTrackingError(ChannelListAction.UnarchiveChannel)
    }

    /**
     * Unmutes a channel.
     *
     * @param channel The channel to unmute.
     */
    public fun unmuteChannel(channel: Channel) {
        dismissChannelAction()

        chatClient.unmuteChannel(channel.type, channel.id)
            .enqueueTrackingError(ChannelListAction.UnmuteChannel)
    }

    /**
     * Deletes a channel, after the user chooses the delete [ChannelAction]. It also removes the
     * [activeChannelAction], to remove the dialog from the UI.
     *
     * @param channel The channel to delete.
     */
    public fun deleteConversation(channel: Channel) {
        dismissChannelAction()

        chatClient.channel(channel.cid).delete().toUnitCall().enqueue { result ->
            when (result) {
                is Result.Success -> _events.tryEmit(ChannelListEvent.ChannelDeleted)
                is Result.Failure -> _events.tryEmit(ChannelListEvent.ActionError(ChannelListAction.DeleteChannel))
            }
        }
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
            chatClient.channel(channel.type, channel.id)
                .removeMembers(listOf(user.id))
                .enqueueTrackingError(ChannelListAction.LeaveGroup)
        }
    }

    /**
     * Mutes a user (used for DM channels).
     *
     * @param userId The ID of the user to mute.
     */
    public fun muteUser(userId: String) {
        dismissChannelAction()
        chatClient.muteUser(userId)
            .enqueueTrackingError(ChannelListAction.MuteUser)
    }

    /**
     * Unmutes a user (used for DM channels).
     *
     * @param userId The ID of the user to unmute.
     */
    public fun unmuteUser(userId: String) {
        dismissChannelAction()
        chatClient.unmuteUser(userId)
            .enqueueTrackingError(ChannelListAction.UnmuteUser)
    }

    /**
     * Blocks a user (used for DM channels).
     *
     * @param userId The ID of the user to block.
     */
    public fun blockUser(userId: String) {
        dismissChannelAction()
        chatClient.blockUser(userId)
            .enqueueTrackingError(ChannelListAction.BlockUser)
    }

    /**
     * Unblocks a user (used for DM channels).
     *
     * @param userId The ID of the user to unblock.
     */
    public fun unblockUser(userId: String) {
        dismissChannelAction()
        chatClient.unblockUser(userId)
            .enqueueTrackingError(ChannelListAction.UnblockUser)
    }

    /**
     * Enqueues this call, emitting a [ChannelListEvent.ActionError] for [action] if it fails.
     */
    private fun <T : Any> Call<T>.enqueueTrackingError(action: ChannelListAction) {
        enqueue { result ->
            if (result is Result.Failure) {
                _events.tryEmit(ChannelListEvent.ActionError(action))
            }
        }
    }

    /**
     * Checks if the channel is muted for the current user.
     *
     * @param cid The CID of the channel that needs to be checked.
     * @return True if the channel is muted for the current user.
     */
    public fun isChannelMuted(cid: String): Boolean = channelMutes.value.any { cid == it.channel?.cid }

    /**
     * Checks if a user is muted by the current user.
     *
     * @param userId The ID of the user to check.
     * @return True if the user is muted.
     */
    public fun isUserMuted(userId: String): Boolean = globalMuted.value.any { it.target?.id == userId }

    /**
     * Checks if a user is blocked by the current user.
     *
     * @param userId The ID of the user to check.
     * @return True if the user is blocked.
     */
    public fun isUserBlocked(userId: String): Boolean = globalBlockedUserIds.value.contains(userId)

    /**
     * Dismisses the [activeChannelAction] and removes it from the UI.
     */
    public fun dismissChannelAction() {
        activeChannelAction = null
        selectedChannel.value = null
    }

    /**
     * Makes the initial query to request channels and starts observing state changes.
     */
    private suspend fun init() {
        logger.d { "[init] no args" }
        val activeQuery: Flow<SearchQuery> = when (mode) {
            is QueryMode.Standard -> combine(_searchQuery, queryConfigFlow, refreshFlow) { query, _, _ -> query }
            is QueryMode.Predefined -> combine(_searchQuery, refreshFlow) { query, _ -> query }
            is QueryMode.Grouped -> combine(_searchQuery, refreshFlow) { query, _ -> query }
        }
        activeQuery.collectLatest { query ->
            logger.i { "[observeInit] query: $query" }
            when (query) {
                is SearchQuery.Empty,
                is SearchQuery.Channels,
                -> {
                    searchScope.coroutineContext.cancelChildren()
                    when (mode) {
                        is QueryMode.Standard,
                        is QueryMode.Predefined,
                        -> {
                            // Standard QueryChannels
                            observeQueryChannels(query.query)
                        }
                        is QueryMode.Grouped ->
                            if (query.query.length >= MIN_CHANNEL_SEARCH_QUERY_LENGTH) {
                                // Standard QueryChannels (with purpose of searching channels)
                                observeQueryChannels(query.query)
                            } else {
                                // GroupedQueryChannels -> just observe underlying state
                                observeGroupedChannels(mode.groupKey)
                            }
                    }
                }
                is SearchQuery.Messages -> {
                    chListScope.coroutineContext.cancelChildren()
                    handleSearchQuery(query.query)
                    observeSearchMessages(query.query)
                }
            }
        }
    }

    private suspend fun observeSearchMessages(query: String) = runCatching {
        logger.d { "[observeSearchMessages] query: '$query'" }
        searchMessageState.filterNotNull().collectLatest {
            logger.v { "[observeSearchMessages] state: ${it.stringify()}" }
            val channels = chatClient.repositoryFacade.selectChannels(it.messages.map { message -> message.cid })
            channelsState = channelsState.copy(
                searchQuery = _searchQuery.value,
                isLoading = it.isLoading,
                isLoadingMore = it.isLoadingMore,
                endOfChannels = !it.canLoadMore,
                channelItems = it.messages.map {
                    val channel = channels.firstOrNull { channel -> channel.cid == it.cid }
                    ItemState.SearchResultItemState(
                        message = it,
                        channel = channel,
                    )
                },
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
        val channelFilter = messageSearchChannelFilter() ?: run {
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
        val channelFilter = messageSearchChannelFilter() ?: run {
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
        val limit = channelLimit
        val next = currentState.next
        logger.v {
            "[searchMessages] #$src; query: '${currentState.query}', sort: $messageSearchSort, next: $next, " +
                "limit: $limit"
        }
        val result = chatClient.searchMessages(
            channelFilter = channelFilter,
            messageFilter = Filters.autocomplete("text", currentState.query),
            sort = messageSearchSort,
            limit = limit,
            next = next,
        ).await()
        return when (result) {
            is io.getstream.result.Result.Success -> {
                logger.v { "[searchMessages] #$src; completed(messages.size: ${result.value.messages.size})" }
                currentState.copy(
                    messages = currentState.messages + result.value.messages,
                    isLoading = false,
                    isLoadingMore = false,
                    canLoadMore = !result.value.next.isNullOrEmpty(),
                    next = result.value.next,
                )
            }

            is io.getstream.result.Result.Failure -> {
                logger.e { "[searchMessages] #$src; failed: ${result.value}" }
                currentState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                )
            }
        }
    }

    /**
     * Creates a [QueryChannelsState] by issuing a remote queryChannels request built from the
     * given [searchQuery] (via [buildQueryChannelsRequest]) and starts collecting from it.
     */
    private fun observeQueryChannels(searchQuery: String) =
        observeQueryChannelsInternal(tag = "observeQueryChannels") {
            val request = buildQueryChannelsRequest(searchQuery) ?: return@observeQueryChannelsInternal null
            chatClient.queryChannelsAsState(
                request = request,
                chatEventHandlerFactory = chatEventHandlerFactory,
                coroutineScope = chListScope,
            )
        }

    /**
     * Subscribes to the identifier-keyed [QueryChannelsState] for the Grouped variant identified
     * by [groupKey], without triggering a remote API call. State is populated externally by
     * `queryGroupedChannels` responses routed through the listener.
     */
    private fun observeGroupedChannels(groupKey: String) =
        observeQueryChannelsInternal(tag = "observeGroupedChannels") {
            chatClient.initGroupedQueryChannelsAsState(
                identifier = QueryChannelsIdentifier.Grouped(groupKey),
                chatEventHandlerFactory = chatEventHandlerFactory,
                coroutineScope = chListScope,
            )
        }

    /**
     * Shared implementation for observing a [QueryChannelsState] from a [createState] producer.
     */
    private fun observeQueryChannelsInternal(
        tag: String,
        createState: () -> StateFlow<QueryChannelsState?>?,
    ) = runCatching {
        queryChannelDebouncer.submitSuspendable {
            queryChannelsState = createState() ?: return@submitSuspendable
            queryChannelsState.filterNotNull().collectLatest { queryChannelsState ->
                combine(
                    queryChannelsState.channelsStateData,
                    channelMutes,
                    typingChannels,
                    channelDraftMessages,
                    globalMuted,
                ) { state, channelMutes, typingChannels, channelDraftMessages, userMutes ->
                    when (state) {
                        ChannelsStateData.NoQueryActive,
                        ChannelsStateData.Loading,
                        -> {
                            logger.d { "[observeQueryChannels] state: Loading" }
                            channelsState.copy(isLoading = true, searchQuery = _searchQuery.value)
                        }

                        ChannelsStateData.OfflineNoResults -> {
                            logger.v { "[observeQueryChannels] state: OfflineNoResults(channels are empty)" }
                            channelsState.copy(
                                isLoading = false,
                                channelItems = emptyList(),
                                searchQuery = _searchQuery.value,
                            )
                        }

                        is ChannelsStateData.Result -> {
                            logger.v { "[$tag] state: Result(channels.size: ${state.channels.size})" }
                            channelsState.copy(
                                isLoading = false,
                                channelItems = createChannelItems(
                                    channels = state.channels,
                                    channelMutes = channelMutes,
                                    userMutes = userMutes,
                                    typingEvents = typingChannels,
                                    draftMessages = channelDraftMessages.takeIf { draftMessagesEnabled } ?: emptyMap(),
                                ),
                                isLoadingMore = false,
                                endOfChannels = queryChannelsState.endOfChannels.value,
                                searchQuery = _searchQuery.value,
                            )
                        }
                    }
                }.collectLatest { newState -> channelsState = newState }
            }
        }
    }.onFailure {
        when (it is CancellationException) {
            true -> logger.v { "[$tag] cancelled" }
            else -> logger.e { "[$tag] failed: $it" }
        }
    }

    /**
     * Builds a [QueryChannelsRequest] for the current [mode] and [searchQuery]. Returns `null` in Standard
     * mode when the filter has not yet been resolved (e.g. before [buildDefaultFilter] completes); in that
     * case the caller should skip the request — the next emission of [filterFlow] will re-trigger.
     *
     * In Predefined mode with an active channel search, falls back to a Standard request whose filter is
     * just [searchChannelFilter] (the predefined filter is server-owned and cannot be combined locally).
     */
    private fun buildQueryChannelsRequest(searchQuery: String): QueryChannelsRequest? = when (val mode = mode) {
        is QueryMode.Standard -> {
            val baseFilter = filterFlow.value ?: return null
            QueryChannelsRequest(
                filter = createQueryChannelsFilter(baseFilter, searchQuery),
                querySort = querySortFlow.value,
                limit = channelLimit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
        }
        is QueryMode.Predefined -> if (searchQuery.length >= MIN_CHANNEL_SEARCH_QUERY_LENGTH) {
            QueryChannelsRequest(
                filter = optimizedChannelSearchFilter(searchQuery),
                limit = channelLimit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
        } else {
            QueryChannelsRequest(
                predefinedFilter = mode.name,
                filterValues = mode.filterValues,
                sortValues = mode.sortValues,
                limit = channelLimit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
        }
        // When in Grouped mode, this is reached only when search is active
        is QueryMode.Grouped -> QueryChannelsRequest(
            filter = optimizedChannelSearchFilter(searchQuery),
            limit = channelLimit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
        )
    }

    /**
     * Creates a filter that is used to query channels.
     *
     * If the [searchQuery] is shorter than 3 characters, then returns the original [filter] provided by the user.
     * Otherwise, returns a wrapped [filter] that also checks that the channel name match the
     * [searchQuery].
     *
     * @param filter The filter that was passed by the user.
     * @param searchQuery The search query used to filter the channels.
     *
     * @return The filter that will be used to query channels.
     */
    @Suppress("SpreadOperator")
    private fun createQueryChannelsFilter(filter: FilterObject, searchQuery: String): FilterObject {
        return if (searchQuery.length >= MIN_CHANNEL_SEARCH_QUERY_LENGTH) {
            if (filter is AndFilterObject) {
                // If the base filter is `AND`, extend it with the search query filter.
                val filters = filter.filterObjects
                val extendedFilters = filters + channelSearchFilter(searchQuery)
                Filters.and(*extendedFilters.toTypedArray())
            } else {
                // If the base filter is not `AND`, wrap it in an `AND` with the search query filter.
                Filters.and(filter, channelSearchFilter(searchQuery))
            }
        } else {
            filter
        }
    }

    private suspend fun loadMoreQueryChannels() {
        logger.d { "[loadMoreQueryChannels] no args" }

        // Grouped + no active channel search uses cursor pagination via queryGroupedChannels.
        if (mode is QueryMode.Grouped &&
            _searchQuery.value.query.length < MIN_CHANNEL_SEARCH_QUERY_LENGTH
        ) {
            loadMoreGroupedChannels(mode.groupKey)
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
        val nextQuery = when (mode) {
            is QueryMode.Standard -> {
                val currentFilter = filterFlow.value ?: run {
                    logger.v { "[loadMoreQueryChannels] rejected (no current filter)" }
                    return
                }
                currentQuery.copy(
                    filter = createQueryChannelsFilter(currentFilter, _searchQuery.value.query),
                    querySort = querySortFlow.value,
                )
            }
            is QueryMode.Predefined -> currentQuery
            is QueryMode.Grouped -> currentQuery
        }
        if (lastNextQuery == nextQuery) {
            logger.v { "[loadMoreQueryChannels] rejected (same query)" }
            return
        }
        lastNextQuery = nextQuery
        logger.v { "[loadMoreQueryChannels] offset: ${nextQuery.offset}, limit: ${nextQuery.limit}" }
        // Preserve loadingError until the outcome is known, so a failing retry doesn't clear and re-set it.
        channelsState = channelsState.copy(isLoadingMore = true)
        val result = chatClient.queryChannels(nextQuery).await()
        if (result.isSuccess) {
            logger.v { "[loadMoreQueryChannels] completed; channels.size: ${result.getOrNull()?.size}" }
            channelsState = channelsState.copy(isLoadingMore = false, loadingError = false)
        } else {
            logger.e { "[loadMoreQueryChannels] failed: ${result.errorOrNull()}" }
            // Clear the cached query so a retry re-issues this page instead of being rejected as a duplicate.
            lastNextQuery = null
            channelsState = channelsState.copy(isLoadingMore = false, loadingError = true)
        }
    }

    private suspend fun loadMoreGroupedChannels(groupKey: String) {
        logger.d { "[loadMoreGroupedChannels] groupKey: $groupKey" }
        val state = queryChannelsState.value
        if (state == null) {
            logger.v { "[loadMoreGroupedChannels] rejected (no current state)" }
            return
        }
        val cursor = state.nextCursor.value
        if (cursor == null) {
            logger.v { "[loadMoreGroupedChannels] rejected (no next cursor)" }
            return
        }
        if (channelsState.endOfChannels) {
            logger.v { "[loadMoreGroupedChannels] rejected (end of channels)" }
            return
        }
        if (channelsState.isLoadingMore) {
            logger.v { "[loadMoreGroupedChannels] rejected (already loading more)" }
            return
        }
        val config = state.groupedQueryConfig.value
        channelsState = channelsState.copy(isLoadingMore = true)
        val result = chatClient.queryGroupedChannelsInternal(
            limit = config?.limit,
            groups = mapOf(
                groupKey to io.getstream.chat.android.models.GroupedChannelsGroupQuery(
                    limit = config?.pageSize,
                    next = cursor,
                ),
            ),
            watch = config?.watch ?: true,
            presence = config?.presence ?: false,
        ).await()
        if (result.isSuccess) {
            logger.v { "[loadMoreGroupedChannels] completed (listener applied)" }
        } else {
            logger.e { "[loadMoreGroupedChannels] failed: ${result.errorOrNull()}" }
        }
        channelsState = channelsState.copy(isLoadingMore = false)
    }

    /**
     * Creates a list of [ItemState.ChannelItemState] that represents channel items we show in the list of channels.
     *
     * @param channels The channels to show.
     * @param channelMutes The list of channels muted for the current user.
     * @param userMutes The list of users muted by the current user.
     */
    private fun createChannelItems(
        channels: List<Channel>,
        channelMutes: List<ChannelMute>,
        userMutes: List<Mute>,
        typingEvents: Map<String, TypingEvent>,
        draftMessages: Map<String, DraftMessage>,
    ): List<ItemState.ChannelItemState> {
        val mutedChannelIds = channelMutes.map { channelMute -> channelMute.channel?.cid }.toSet()
        val mutedUserIds = userMutes.mapNotNullTo(mutableSetOf()) { it.target?.id }
        val currentUser = user.value
        return channels.map {
            ItemState.ChannelItemState(
                channel = it,
                isMuted = it.cid in mutedChannelIds,
                isUserMuted = it.isOneToOneMutedByUser(currentUser, mutedUserIds),
                typingUsers = typingEvents[it.cid]?.users ?: emptyList(),
                draftMessage = draftMessages[it.cid],
            )
        }
    }

    /** Checks if a 1:1 channel is muted via user mute (i.e. the other member is muted). */
    private fun Channel.isOneToOneMutedByUser(currentUser: User?, mutedUserIds: Set<String>): Boolean =
        if (mutedUserIds.isEmpty() || !isOneToOne(currentUser)) {
            false
        } else {
            members.any { it.user.id != currentUser?.id && it.user.id in mutedUserIds }
        }

    /**
     * Builds the default channel filter, which represents "messaging" channels that the current user is a part of.
     */
    private fun defaultChannelsFilter(): Flow<FilterObject> =
        chatClient.clientState.user.map(Filters::defaultChannelListFilter).filterNotNull()

    @Deprecated(
        message = "Avoid using this search query as `member.user.name` is an expensive operation. " +
            "In the future, we should migrate to use optimizedChannelSearchFilter.",
        replaceWith = ReplaceWith("optimizedChannelSearchFilter(searchQuery)"),
    )
    private fun channelSearchFilter(searchQuery: String): FilterObject {
        return Filters.or(
            Filters.autocomplete("member.user.name", searchQuery),
            Filters.autocomplete("name", searchQuery),
        )
    }

    private fun optimizedChannelSearchFilter(text: String): FilterObject =
        Filters.and(
            Filters.autocomplete("name", text),
            Filters.`in`("members", user.value?.id.orEmpty()),
        )

    private fun messageSearchChannelFilter(): FilterObject? = when (mode) {
        // Standard mode: Use the initial filters (backwards compatible)
        is QueryMode.Standard -> filterFlow.value ?: Filters.defaultChannelListFilter(user.value)
        // Predefined and Grouped modes: Use simple membership filter (aligned with other platforms);
        is QueryMode.Predefined,
        is QueryMode.Grouped,
        -> when (val userId = user.value?.id) {
            null -> null
            else -> Filters.`in`("members", listOf(userId))
        }
    }

    internal companion object {
        /**
         * Default value of number of channels to return when querying channels.
         */
        internal const val DEFAULT_CHANNEL_LIMIT = 30

        /**
         * Debounce time for search queries.
         */
        internal const val SEARCH_DEBOUNCE_MS = 300L

        /**
         * Minimum length of the search query to start searching for channels.
         */
        private const val MIN_CHANNEL_SEARCH_QUERY_LENGTH = 3
    }

    private data class SearchMessageState(
        val query: String = "",
        val canLoadMore: Boolean = true,
        val next: String? = null,
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
                "canLoadMore=$canLoadMore, " +
                "next=$next)"
        }
    }
}
