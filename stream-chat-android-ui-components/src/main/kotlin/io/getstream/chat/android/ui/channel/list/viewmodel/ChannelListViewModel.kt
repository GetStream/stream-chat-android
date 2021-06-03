package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.enqueue
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.isMuted
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map

/**
 * ViewModel class for [io.getstream.chat.android.ui.channel.list.ChannelListView].
 * Responsible for keeping the channels list up to date.
 * Can be bound to the view using [ChannelListViewModel.bindView] function.
 * @param chatDomain entry point for all livedata & offline operations
 * @param filter filter for querying channels, should never be empty
 * @param sort defines the ordering of the channels
 * @param limit the maximum number of channels to fetch
 * @param messageLimit the number of messages to fetch for each channel
 */
public class ChannelListViewModel(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        userFilter(chatDomain),
        Filters.or(Filters.notExists("draft"), Filters.ne("draft", true)),
    ),
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30,
    messageLimit: Int = 1,
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    public val state: LiveData<State> = stateMerger
    public val typingEvents: LiveData<TypingEvent>
        get() = chatDomain.typingUpdates.asLiveData()

    private val paginationStateMerger = MediatorLiveData<PaginationState>()
    public val paginationState: LiveData<PaginationState> = Transformations.distinctUntilChanged(paginationStateMerger)
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    public val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    init {
        stateMerger.value = INITIAL_STATE

        chatDomain.queryChannels(filter, sort, limit, messageLimit).enqueue { queryChannelsControllerResult ->
            if (queryChannelsControllerResult.isSuccess) {
                val queryChannelsController = queryChannelsControllerResult.data()

                val channelState = queryChannelsController.channelsState.flatMapConcat { channelState ->
                    chatDomain.user.filterNotNull().map { currentUser ->
                        channelState to currentUser
                    }
                }.map { (channelState, currentUser) ->
                    handleChannelState(channelState, currentUser)
                }.asLiveData()


                stateMerger.addSource(channelState) { state -> stateMerger.value = state }

                stateMerger.addSource(queryChannelsController.mutedChannelIds.asLiveData()) { mutedChannels ->
                    val state = stateMerger.value

                    if (state is State.Result) {
                        stateMerger.value = state.copy(channels = parseMutedChannels(state.channels, mutedChannels))
                    } else if (state is State.Error) {
                        stateMerger.value = state.copy()
                    }
                }

                paginationStateMerger.addSource(queryChannelsController.loadingMore.asLiveData()) { loadingMore ->
                    setPaginationState { copy(loadingMore = loadingMore) }
                }
                paginationStateMerger.addSource(queryChannelsController.endOfChannels.asLiveData()) { endOfChannels ->
                    setPaginationState { copy(endOfChannels = endOfChannels) }
                }
            } else {
                stateMerger.value = State.Error("Query failed")
            }
        }
    }

    private fun handleChannelState(
        channelState: QueryChannelsController.ChannelsState,
        currentUser: User)
    : State {
        return when (channelState) {
            is QueryChannelsController.ChannelsState.NoQueryActive,
            is QueryChannelsController.ChannelsState.Loading,
            -> State.Result(isLoading = true, emptyList())
            is QueryChannelsController.ChannelsState.OfflineNoResults -> State.Result(
                isLoading = false,
                channels = emptyList(),
            )
            is QueryChannelsController.ChannelsState.Result ->
                State.Result(
                    isLoading = false,
                    channels = parseMutedChannels(
                        channelState.channels,
                        currentUser.channelMutes.map { channelMute -> channelMute.channel.id }
                    ),
                )
        }
    }

    public fun onAction(action: Action) {
        when (action) {
            is Action.ReachedEndOfList -> requestMoreChannels()
        }.exhaustive
    }

    public fun leaveChannel(channel: Channel) {
        chatDomain.leaveChannel(channel.cid).enqueue(
            onError = { _errorEvents.postValue(Event(ErrorEvent.LeaveChannelError(it))) }
        )
    }

    public fun deleteChannel(channel: Channel) {
        chatDomain.deleteChannel(channel.cid).enqueue(
            onError = { _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError(it))) }
        )
    }

    public fun hideChannel(channel: Channel) {
        chatDomain.hideChannel(channel.cid, true).enqueue(
            onError = { _errorEvents.postValue(Event(ErrorEvent.HideChannelError(it))) }
        )
    }

    public fun markAllRead() {
        chatDomain.markAllRead().enqueue()
    }

    private fun requestMoreChannels() {
        chatDomain.queryChannelsLoadMore(filter, sort).enqueue()
    }

    private fun setPaginationState(reducer: PaginationState.() -> PaginationState) {
        paginationStateMerger.value = reducer(paginationStateMerger.value ?: PaginationState())
    }

    public sealed class State {
        public data class Result(val isLoading: Boolean, val channels: List<Channel>) : State()
        public data class Error(val message: String) : State()
    }

    private fun parseMutedChannels(
        channelsMap: List<Channel>,
        channelMutesIds: List<String>?,
    ): List<Channel> {
        return channelsMap.map { channel ->
            channel.copy().apply {
                isMuted = channelMutesIds?.contains(channel.id) ?: false
            }
        }
    }

    public data class PaginationState(
        val loadingMore: Boolean = false,
        val endOfChannels: Boolean = false,
    )

    public sealed class Action {
        public object ReachedEndOfList : Action()
    }

    public sealed class ErrorEvent(public open val chatError: ChatError) {
        public data class LeaveChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
        public data class DeleteChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
        public data class HideChannelError(override val chatError: ChatError) : ErrorEvent(chatError)
    }

    public companion object {
        @JvmField
        public val DEFAULT_SORT: QuerySort<Channel> = QuerySort.desc("last_updated")

        private val INITIAL_STATE: State = State.Result(isLoading = true, channels = emptyList())
    }

}

private fun userFilter(chatDomain: ChatDomain): FilterObject {
    return chatDomain.user.value?.id?.let { id ->
        Filters.`in`("members", id)
    } ?: Filters.neutral().also {
        ChatLogger.get("ChannelListViewModel")
            .logE("User is not set in ChatDomain, default filter for ChannelListViewModel won't work")
    }
}
