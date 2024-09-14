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
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.viewmodel.channels.delegates.StreamChannelSearchHelper
import io.getstream.chat.android.compose.viewmodel.channels.usecases.SearchChannelsForQuery
import io.getstream.chat.android.compose.viewmodel.channels.usecases.SearchMessagesForQuery
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.ui.common.state.channels.actions.Cancel
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.uiutils.extension.defaultChannelListFilter
import io.getstream.log.taggedLogger
import io.getstream.result.call.toUnitCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch

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
public class ChannelListViewModel(
    public val chatClient: ChatClient,
    initialSort: QuerySorter<Channel>,
    private val initialFilters: FilterObject?,
    private val channelLimit: Int = DEFAULT_CHANNEL_LIMIT,
    private val memberLimit: Int = DEFAULT_MEMBER_LIMIT,
    private val messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(chatClient.clientState),
    searchDebounceMs: Long = SEARCH_DEBOUNCE_MS,
) : ViewModel(),
    IChannelViewState by ChannelViewStateImpl(chatClient, initialSort, initialFilters) {

    private val logger by taggedLogger("Chat:ChannelListVM")

    private val streamSearchHelper by lazy {
        StreamChannelSearchHelper(
            searchDebounceMs,
            viewModelScope = viewModelScope
        )
    }

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

    private val searchChannelsForQuery by lazy {
        SearchChannelsForQuery(
            channelLimit = channelLimit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
            chatClient = chatClient,
            chatEventHandlerFactory = chatEventHandlerFactory,
            channelState = this,
            logger = logger,
            iHelpSearchWithDebounce = streamSearchHelper,
        )
    }

    private val searchMessagesForQuery by lazy {
        SearchMessagesForQuery(
            chatClient = chatClient,
            logger = logger,
            channelLimit = channelLimit,
            iChannelViewState = this,
            iHelpSearchWithDebounce = streamSearchHelper,
        )
    }

    /**
     * Combines the latest search query and filter to fetch channels and emit them to the UI.
     */
    init {
        setupFilters()
        setupSearchAndQuery()
    }

    private fun setupFilters() {
        if (initialFilters == null) {
            viewModelScope.launch {
                val filter = buildDefaultFilter().first()
                filterFlow.value = filter
            }
        }
    }

    /**
     * Makes the initial query to request channels and starts observing state changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun setupSearchAndQuery() {
        logger.d { "[init] no args" }
        searchQuery.combine(queryConfigFlow) { query, config -> query to config }
            .mapLatest { (query, config) ->
                when (query) {
                    is SearchQuery.Empty,
                    is SearchQuery.Channels,
                    -> {
                        searchChannelsForQuery(
                            config = query.getConfig(config),
                        )
                    }

                    is SearchQuery.Messages -> {
                        searchMessagesForQuery(
                            query = query.query,
                        )
                    }
                }
            }.catch {
                logger.e(it) {
                    "setupSearchAndQuery failed"
                }
            }.launchIn(viewModelScope)
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
            -> searchChannelsForQuery.loadMoreQueryChannels()

            is SearchQuery.Messages,
            -> searchMessagesForQuery.loadMoreQueryMessages()
        }
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
}
