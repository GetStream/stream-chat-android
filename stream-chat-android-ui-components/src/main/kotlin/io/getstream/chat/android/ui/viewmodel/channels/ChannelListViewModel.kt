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

package io.getstream.chat.android.ui.viewmodel.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.errors.extractCause
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.extensions.globalStateFlow
import io.getstream.chat.android.state.extensions.queryChannelsAsState
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.android.ui.feature.channels.list.ChannelListView
import io.getstream.chat.android.ui.utils.extensions.EXTRA_DATA_MUTED
import io.getstream.chat.android.ui.utils.extensions.addFlow
import io.getstream.chat.android.ui.utils.extensions.isMuted
import io.getstream.chat.android.uiutils.extension.defaultChannelListFilter
import io.getstream.log.TaggedLogger
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.call.enqueue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch

/**
 * ViewModel class for [ChannelListView].
 * Responsible for keeping the channels list up to date.
 * Can be bound to the view using [ChannelListViewModel.bindView] function.
 *
 * @param filter Filter for querying channels, should never be empty.
 * @param sort Defines the ordering of the channels.
 * @param limit The maximum number of channels to fetch.
 * @param messageLimit The number of messages to fetch for each channel.
 * @param memberLimit The number of members to fetch per channel.
 * @param isDraftMessagesEnabled Enables or disables draft messages.
 * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
 * @param chatClient Entry point for all low-level operations.
 * @param globalState A flow emitting the current [GlobalState].
 */
@OptIn(ExperimentalCoroutinesApi::class)
public class ChannelListViewModel(
    private val filter: FilterObject? = null,
    private val sort: QuerySorter<Channel> = DEFAULT_SORT,
    private val limit: Int = DEFAULT_CHANNEL_LIMIT,
    private val messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
    private val memberLimit: Int = DEFAULT_MEMBER_LIMIT,
    private val isDraftMessagesEnabled: Boolean,
    private val chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(),
    private val chatClient: ChatClient = ChatClient.instance(),
    private val globalState: Flow<GlobalState> = chatClient.globalStateFlow,
) : ViewModel() {

    /**
     * ViewModel class for [ChannelListView].
     * Responsible for keeping the channels list up to date.
     * Can be bound to the view using [ChannelListViewModel.bindView] function.
     *
     * @param filter Filter for querying channels, should never be empty.
     * @param sort Defines the ordering of the channels.
     * @param limit The maximum number of channels to fetch.
     * @param messageLimit The number of messages to fetch for each channel.
     * @param memberLimit The number of members to fetch per channel.
     * @param isDraftMessagesEnabled Enables or disables draft messages.
     * @param chatEventHandlerFactory The instance of [ChatEventHandlerFactory] that will be used to create [ChatEventHandler].
     * @param chatClient Entry point for all low-level operations.
     * @param globalState The current [GlobalState].
     */
    @Deprecated("Use the constructor which accepts a Flow<GlobalState> for the globalState instead.")
    public constructor(
        filter: FilterObject? = null,
        sort: QuerySorter<Channel> = DEFAULT_SORT,
        limit: Int = DEFAULT_CHANNEL_LIMIT,
        messageLimit: Int = DEFAULT_MESSAGE_LIMIT,
        memberLimit: Int = DEFAULT_MEMBER_LIMIT,
        isDraftMessagesEnabled: Boolean = true,
        chatEventHandlerFactory: ChatEventHandlerFactory = ChatEventHandlerFactory(),
        chatClient: ChatClient = ChatClient.instance(),
        globalState: GlobalState = chatClient.globalState,
    ) : this(
        filter = filter,
        sort = sort,
        limit = limit,
        messageLimit = messageLimit,
        memberLimit = memberLimit,
        isDraftMessagesEnabled = isDraftMessagesEnabled,
        chatEventHandlerFactory = chatEventHandlerFactory,
        chatClient = chatClient,
        globalState = MutableStateFlow(globalState),
    )

    private var queryJob: Job? = null

    /**
     * Represents the current state containing channel list
     * information that is a product of multiple sources.
     */
    private val stateMerger = MediatorLiveData<State>()

    /**
     * Represents the current state containing channel list information.
     */
    public val state: LiveData<State> = stateMerger.distinctUntilChanged()

    /**
     * Updates about currently typing users in active channels.
     *
     * @see [GlobalState.typingChannels]
     */
    public val typingEvents: LiveData<Map<String, TypingEvent>> = globalState
        .flatMapLatest { it.typingChannels }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
        .asLiveData()

    /**
     * Draft messages for channels.
     */
    public val draftMessages: LiveData<Map<String, DraftMessage>> = if (isDraftMessagesEnabled) {
        globalState
            .flatMapLatest { it.channelDraftMessages }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
            .asLiveData()
    } else {
        MutableLiveData(emptyMap())
    }

    private val channelMutes: StateFlow<List<ChannelMute>> = globalState
        .flatMapLatest { it.channelMutes }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * Represents the current pagination state that is a product
     * of multiple sources.
     */
    private val paginationStateMerger = MediatorLiveData<PaginationState>()

    /**
     * Represents the current pagination state by containing
     * information about the loading state and if we have
     * reached the end of all available channels.
     */
    public val paginationState: LiveData<PaginationState> = paginationStateMerger.distinctUntilChanged()

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
    private val logger: TaggedLogger by taggedLogger("Chat:ChannelList-VM")

    /**
     * Filters the requested channels.
     */
    private val filterLiveData: MutableLiveData<FilterObject?> = MutableLiveData(filter)

    /**
     * Represents the current state of the channels query.
     */
    private var queryChannelsState: StateFlow<QueryChannelsState?> = MutableStateFlow(null)

    init {
        if (filter == null) {
            viewModelScope.launch {
                val filter = buildDefaultFilter().first()

                this@ChannelListViewModel.filterLiveData.value = filter
            }
        }

        stateMerger.addSource(filterLiveData) { filter ->
            if (filter != null) {
                initData(filter)
            }
        }
    }

    /**
     * Builds the default channel filter, which represents "messaging" channels that the current user is a part of.
     */
    private fun buildDefaultFilter(): Flow<FilterObject> {
        return chatClient.clientState.user.map(Filters::defaultChannelListFilter).filterNotNull()
    }

    /**
     * Initializes the data necessary for the screen.
     */
    private fun initData(filterObject: FilterObject) {
        stateMerger.value = INITIAL_STATE
        init(filterObject)
    }

    /**
     * Initializes this ViewModel with OfflinePlugin implementation. It makes the initial query to request channels
     * and starts to observe state changes.
     */
    private fun init(filterObject: FilterObject) {
        val queryChannelsRequest =
            QueryChannelsRequest(
                filter = filterObject,
                querySort = sort,
                limit = limit,
                messageLimit = messageLimit,
                memberLimit = memberLimit,
            )
        queryChannelsState =
            chatClient.queryChannelsAsState(queryChannelsRequest, chatEventHandlerFactory, viewModelScope)

        /**
         * We clean up any previous loads to make sure the current one is the only one running.
         */
        queryJob?.cancel()
        val queryJob = Job(viewModelScope.coroutineContext.job).also {
            this.queryJob = it
        }

        viewModelScope.launch(queryJob) {
            queryChannelsState.filterNotNull().collectLatest { queryChannelsState ->
                if (!isActive) {
                    return@collectLatest
                }
                stateMerger.addFlow(queryJob, queryChannelsState.channelsStateData) { channelsState ->
                    stateMerger.value = handleChannelStateNews(channelsState, channelMutes.value)
                }
                stateMerger.addFlow(queryJob, channelMutes) { channelMutes ->
                    val state = stateMerger.value

                    if (state?.channels?.isNotEmpty() == true) {
                        stateMerger.value = state.copy(channels = parseMutedChannels(state.channels, channelMutes))
                    } else {
                        stateMerger.value = state?.copy()
                    }
                }

                paginationStateMerger.addFlow(queryJob, queryChannelsState.loadingMore) { loadingMore ->
                    setPaginationState { copy(loadingMore = loadingMore) }
                }
                paginationStateMerger.addFlow(queryJob, queryChannelsState.endOfChannels) { endOfChannels ->
                    setPaginationState { copy(endOfChannels = endOfChannels) }
                }
            }
        }
    }

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
        chatClient.clientState.user.value?.let { user ->
            val channelClient = chatClient.channel(channel.type, channel.id)
            channelClient.removeMembers(listOf(user.id)).enqueue(
                onError = { error ->
                    logger.e {
                        "Could not leave channel with id: ${channel.id}. " +
                            "Error: ${error.message}. Cause: ${error.extractCause()}"
                    }
                    _errorEvents.postValue(Event(ErrorEvent.LeaveChannelError(error)))
                },
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
            onError = { error ->
                logger.e {
                    "Could not delete channel with id: ${channel.id}. " +
                        "Error: ${error.message}. Cause: ${error.extractCause()}"
                }
                _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError(error)))
            },
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
            clearHistory = false,
        ).enqueue(
            onError = { error ->
                logger.e {
                    "Could not hide channel with id: ${channel.id}. " +
                        "Error: ${error.message}. Cause: ${error.extractCause()}"
                }
                _errorEvents.postValue(Event(ErrorEvent.HideChannelError(error)))
            },
        )
    }

    /**
     * Marks all of the channels as read.
     */
    public fun markAllRead() {
        chatClient.markAllRead().enqueue(
            onError = { streamError ->
                logger.e {
                    "Could not mark all messages as read. " +
                        "Error: ${streamError.message}. Cause: ${streamError.extractCause()}"
                }
            },
        )
    }

    /**
     * Requests more channels.
     * Called when scrolling to the end of the list.
     */
    private fun requestMoreChannels() {
        filterLiveData.value?.let {
            val queryChannelsState = queryChannelsState.value ?: return

            queryChannelsState.nextPageRequest.value?.let {
                viewModelScope.launch {
                    chatClient.queryChannels(it).enqueue(
                        onError = { streamError ->
                            logger.e {
                                "Could not load more channels. Error: ${streamError.message}. " +
                                    "Cause: ${streamError.extractCause()}"
                            }
                        },
                    )
                }
            }
        }
    }

    /**
     * Allows us to change the filter based on our requirements.
     *
     * @param filterObject The new filter to be applied to the query which lets us fetch different data.
     */
    public fun setFilters(filterObject: FilterObject) {
        logger.d { "[setFilters] filterObject: $filterObject" }
        this.filterLiveData.value = filterObject
    }

    /**
     * Sets the current pagination state.
     *
     * @param reducer A lambda function that returns [PaginationState].
     */
    private fun setPaginationState(reducer: PaginationState.() -> PaginationState) {
        paginationStateMerger.value = reducer(paginationStateMerger.value ?: PaginationState())
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
        val mutedChannelsIds = channelMutes.map { channelMute -> channelMute.channel?.id.orEmpty() }.toSet()
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
        public object ReachedEndOfList : Action() {
            override fun toString(): String = "ReachedEndOfList"
        }
    }

    /**
     * Describes the actions that were taken.
     */
    public sealed class ErrorEvent(public open val streamError: Error) {

        /**
         * Event for errors upon leaving a channel.
         *
         * @param streamError Contains error data such as a [Throwable] and a message.
         */
        public data class LeaveChannelError(override val streamError: Error) : ErrorEvent(streamError)

        /**
         * Event for errors upon deleting a channel.
         *
         * @param streamError Contains error data such as a [Throwable] and a message.
         */
        public data class DeleteChannelError(override val streamError: Error) : ErrorEvent(streamError)

        /**
         * Event for errors upon hiding a channel.
         *
         * @param streamError Contains error data such as a [Throwable] and a message.
         */
        public data class HideChannelError(override val streamError: Error) : ErrorEvent(streamError)
    }

    public companion object {

        /**
         * The default sorting option for queries.
         */
        @JvmField
        public val DEFAULT_SORT: QuerySorter<Channel> = QuerySortByField.descByName("last_updated")

        /**
         *  The initial state.
         */
        private val INITIAL_STATE: State = State(isLoading = true, channels = emptyList())

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
