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

package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.getstream.sdk.chat.state.QueryConfig
import com.getstream.sdk.chat.utils.extensions.defaultUserFilterFlow
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.logger.TaggedLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.event.handler.chat.ChatEventHandler
import io.getstream.chat.android.offline.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.queryChannelsAsState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.ui.common.extensions.internal.EXTRA_DATA_MUTED
import io.getstream.chat.android.ui.common.extensions.internal.isMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.ChannelListView].
 * Responsible for keeping the channels list up to date.
 * Can be bound to the view using [ChannelListViewModel.bindView] function.
 *
 * @param filter Filter for querying channels, should never be empty.
 * @param sort Defines the ordering of the channels.
 * @param limit The maximum number of channels to fetch.
 * @param messageLimit The number of messages to fetch for each channel.
 * @param memberLimit The number of members to fetch per channel.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
 * @param chatClient Entry point for all low-level operations.
 * @param globalState Global state of OfflinePlugin. Contains information
 * such as the current user, connection state, unread counts etc.
 */
public class ChannelListViewModel(
    private val filter: FilterObject? = null,
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30,
    private val messageLimit: Int = 1,
    private val memberLimit: Int = 30,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(),
    private val chatClient: ChatClient = ChatClient.instance(),
    private val globalState: GlobalState = chatClient.globalState,
) : ViewModel() {

    /**
     * Represents the current state containing channel list
     * information that is a product of multiple sources.
     */
    private val _state = MutableStateFlow(INITIAL_STATE)

    /**
     * Represents the current state containing channel list information.
     */
    public val state: LiveData<State> = _state.asLiveData()

    /**
     * Updates about currently typing users in active channels. See [TypingEvent].
     */
    public val typingEvents: LiveData<TypingEvent>
        get() = globalState.typingUpdates.asLiveData()

    /**
     * Represents the current pagination state that is a product
     * of multiple sources.
     */
    private val _paginationState = MutableStateFlow(PaginationState())

    /**
     * Represents the current pagination state by containing
     * information about the loading state and if we have
     * reached the end of all available channels.
     */
    public val paginationState: LiveData<PaginationState> = _paginationState.asLiveData()

    /**
     * Used to update and emit error events.
     */
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()

    /**
     * Emits error events.
     */
    public val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    /**
     * The logger used to print information, warnings, errors, etc. to log.
     */
    private val logger: TaggedLogger = ChatLogger.get("ChannelListViewModel")

    /**
     * Filters the requested channels.
     */
    private val filtersState: MutableStateFlow<FilterObject?> = MutableStateFlow(filter)

    /**
     * Sorts the requested channels.
     */
    private val querySortState: MutableStateFlow<QuerySort<Channel>> = MutableStateFlow(sort)

    /**
     * Represents the current state of the channels query.
     */
    private var queryChannelsState: StateFlow<QueryChannelsState?>? = MutableStateFlow(null)

    /**
     * The currently active query configuration, stored in a [MutableStateFlow]. It's created using
     * the initial [filter] parameter and [sort] values, but can be changed.
     */
    private val queryConfig = filtersState.filterNotNull().combine(querySortState) { filters, sort ->
        QueryConfig(filters = filters, querySort = sort)
    }

    init {
        observeUser()
        viewModelScope.launch(Dispatchers.IO) {
            if (filter == null) {
                launch {
                    val user = globalState.user.filterNotNull()
                    val filter = defaultUserFilterFlow(user).first()

                    this@ChannelListViewModel.filtersState.value = filter
                }
            }

            queryChannels()
        }
    }

    /**
     * Collects emissions from [queryConfig] and uses them to
     * query channels.
     */
    private suspend fun queryChannels() {
        queryConfig.collectLatest { config ->
            val queryChannelsRequest = QueryChannelsRequest(
                filter = config.filters,
                querySort = config.querySort,
                limit = limit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )

            queryChannelsState = chatClient.queryChannelsAsState(queryChannelsRequest, viewModelScope)
            observeChannels()
        }
    }

    /**
     * Combines channel mutes and [QueryChannelsState] properties
     * necessary for proper channel list function such as the channel state data
     * and pagination related values.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun observeChannels() {
        queryChannelsState?.filterNotNull()
            ?.flatMapLatest { queryChannelsState ->
                combine(
                    globalState.channelMutes,
                    queryChannelsState.channelsStateData,
                    queryChannelsState.loadingMore,
                    queryChannelsState.endOfChannels,
                ) { channelMutes, channelStateData, loadingMoreChannels, endOfChannels ->

                    queryChannelsState.chatEventHandler =
                        chatEventHandlerFactory.chatEventHandler(queryChannelsState.channels)

                    _state.value = handleChannelStateNews(channelStateData, channelMutes)

                    setPaginationState { copy(loadingMore = loadingMoreChannels) }

                    setPaginationState { copy(endOfChannels = endOfChannels) }
                }
            }?.collect()
    }

    /**
     * Initializes this ViewModel with OfflinePlugin implementation. It makes the initial query to request channels
     * and starts to observe state changes.
     */

    /**
     * Handles update about [ChannelsStateData] changes and emit new [State].
     *
     * @param channelState Current state of the channels query.
     * @param channelMutes List of muted channels.
     *
     * @return New [State] after handling channels state changes.
     */
    private fun handleChannelStateNews(
        channelState: ChannelsStateData,
        channelMutes: List<ChannelMute>,
    ): State {
        return when (channelState) {
            is ChannelsStateData.NoQueryActive,
            is ChannelsStateData.Loading,
            -> State(isLoading = true, emptyList())
            is ChannelsStateData.OfflineNoResults -> State(
                isLoading = false,
                channels = emptyList(),
            )
            is ChannelsStateData.Result -> State(
                isLoading = false,
                channels = parseMutedChannels(channelState.channels, channelMutes),
            )
        }
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
        this.filtersState.tryEmit(value = newFilters)
    }

    /**
     * Allows for the change of the query sort used for channel queries.
     *
     * Use this if you need to support runtime sort changes, through custom sort UI.
     */
    public fun setQuerySort(querySort: QuerySort<Channel>) {
        this.querySortState.tryEmit(value = querySort)
    }

    /**
     * Checks against available actions and creates side-effects accordingly.
     *
     * @param action The action to process.
     */
    public fun onAction(action: Action) {
        when (action) {
            is Action.ReachedEndOfList -> requestMoreChannels()
        }
    }

    /**
     * Removes the current user from the channel.
     *
     * @param channel The channel that the current user will leave.
     */
    public fun leaveChannel(channel: Channel) {
        chatClient.getCurrentUser()?.let { user ->
            chatClient.channel(channel.type, channel.id).removeMembers(listOf(user.id)).enqueue(
                onError = { chatError ->
                    logger.logE("Could not leave channel with id: ${channel.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                    _errorEvents.postValue(Event(ErrorEvent.LeaveChannelError(chatError)))
                }
            )
        }
    }

    /**
     * Deletes a channel.
     *
     * @param channel Channel to be deleted.
     */
    public fun deleteChannel(channel: Channel) {
        chatClient.channel(channel.cid).delete().enqueue(
            onError = { chatError ->
                logger.logE("Could not delete channel with id: ${channel.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError(chatError)))
            }
        )
    }

    /**
     * Hides the given channel.
     */
    public fun hideChannel(channel: Channel) {
        val (channelType, channelId) = channel.cid.cidToTypeAndId()
        chatClient.hideChannel(
            channelType = channelType,
            channelId = channelId,
            clearHistory = false
        ).enqueue(
            onError = { chatError ->
                logger.logE("Could not hide channel with id: ${channel.id}. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                _errorEvents.postValue(Event(ErrorEvent.HideChannelError(chatError)))
            }
        )
    }

    /**
     * Marks all of the channels as read.
     */
    public fun markAllRead() {
        chatClient.markAllRead().enqueue(
            onError = { chatError ->
                logger.logE("Could not mark all messages as read. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
            }
        )
    }

    /**
     * Requests more channels.
     * Called when scrolling to the end of the list.
     */
    private fun requestMoreChannels() {
        filtersState.value?.let { filter ->
            val queryChannelsState = queryChannelsState?.value ?: return

            queryChannelsState.nextPageRequest.value?.let {
                viewModelScope.launch {
                    chatClient.queryChannels(it).enqueue(
                        onError = { chatError ->
                            logger.logE("Could not load more channels. Error: ${chatError.message}. Cause: ${chatError.cause?.message}")
                        }
                    )
                }
            }
        }
    }

    /**
     * Observes changes [GlobalState.user].
     *
     * Used to clear the state once the user disconnects.
     */
    private fun observeUser() {
        globalState.user.onEach { user ->
            if (user == null) {
                _state.value = State(
                    channels = listOf(),
                    isLoading = false
                )

                queryChannelsState = null
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Sets the current pagination state.
     *
     * @param reducer A lambda function that returns [PaginationState].
     */
    private fun setPaginationState(reducer: PaginationState.() -> PaginationState) {
        _paginationState.value = reducer(_paginationState.value)
    }

    /**
     * Described the state of the list of channels.
     *
     * @param isLoading If the list is currently loading.
     * @param channels The list of channels to be displayed.
     */
    public data class State(val isLoading: Boolean, val channels: List<Channel>)

    /**
     * Takes in a list of channels and returns the muted ones.
     *
     * @param channels The list of channels to be filtered.
     * @param channelMutes The list of muted channels.
     */
    private fun parseMutedChannels(
        channels: List<Channel>,
        channelMutes: List<ChannelMute>,
    ): List<Channel> {
        val mutedChannelsIds = channelMutes.map { channelMute -> channelMute.channel.id }.toSet()
        return channels.map { channel ->
            when {
                channel.isMuted != channel.id in mutedChannelsIds ->
                    channel.copy(extraData = channel.extraData.clone(EXTRA_DATA_MUTED, !channel.isMuted))

                else -> channel
            }
        }
    }

    /**
     * Clones the given map while changing the given key-value pair.
     */
    private fun <K, V> Map<K, V>.clone(changeKey: K, changeValue: V): MutableMap<K, V> {
        val originalMap = this

        return mutableMapOf<K, V>().apply {
            putAll(originalMap)
            put(changeKey, changeValue)
        }
    }

    /**
     * Describes the pagination state.
     *
     * @param loadingMore If we are currently loading more channels.
     * @param endOfChannels If we have reached the end of all available channels
     * for the current user.
     */
    public data class PaginationState(
        val loadingMore: Boolean = false,
        val endOfChannels: Boolean = false,
    )

    /**
     * Describes the available actions that can be taken.
     */
    public sealed class Action {
        public object ReachedEndOfList : Action()
    }

    /**
     * Describes the actions that were taken.
     */
    public sealed class ErrorEvent(public open val chatError: ChatError) {

        /**
         * Event for errors upon leaving a channel.
         *
         * @param chatError Contains error data such as a [Throwable] and a message.
         */
        public data class LeaveChannelError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * Event for errors upon deleting a channel.
         *
         * @param chatError Contains error data such as a [Throwable] and a message.
         */
        public data class DeleteChannelError(override val chatError: ChatError) : ErrorEvent(chatError)

        /**
         * Event for errors upon hiding a channel.
         *
         * @param chatError Contains error data such as a [Throwable] and a message.
         */
        public data class HideChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
    }

    public companion object {

        /**
         * The default sorting option for queries.
         */
        @JvmField
        public val DEFAULT_SORT: QuerySort<Channel> = QuerySort.desc("last_updated")

        /**
         *  The initial state.
         */
        private val INITIAL_STATE: State = State(isLoading = true, channels = emptyList())
    }
}
