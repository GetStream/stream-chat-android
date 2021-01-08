package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.utils.extensions.isDraft
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController

/**
 * ViewModel class for [com.getstream.sdk.chat.view.channels.ChannelsView].
 * Responsible for keeping the channels list up to date.
 * Can be bound to the view using [ChannelsViewModel.bindView] function.
 * @param chatDomain entry point for all livedata & offline operations
 * @param filter filter for querying channels, should never be empty
 * @param sort defines the ordering of the channels
 * @param limit the maximum number of channels to fetch
 */
public class ChannelsViewModel(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject = Filters.and(
        eq("type", "messaging"),
        Filters.`in`("members", listOf(chatDomain.currentUser.id)),
        Filters.ne("draft", true)
    ),
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    public val state: LiveData<State> = stateMerger
    public val typingEvents: LiveData<TypingEvent>
        get() = chatDomain.typingUpdates

    private val paginationStateMerger = MediatorLiveData<PaginationState>()
    public val paginationState: LiveData<PaginationState> = Transformations.distinctUntilChanged(paginationStateMerger)

    init {
        stateMerger.value = State.Loading
        chatDomain.useCases.queryChannels(filter, sort, limit).enqueue { queryChannelsControllerResult ->
            if (queryChannelsControllerResult.isSuccess) {
                val queryChannelsController = queryChannelsControllerResult.data()
                stateMerger.addSource(
                    map(queryChannelsController.channelsState) { channelState ->
                        when (channelState) {
                            is QueryChannelsController.ChannelsState.NoQueryActive,
                            is QueryChannelsController.ChannelsState.Loading -> State.Loading
                            is QueryChannelsController.ChannelsState.OfflineNoResults -> State.NoChannelsAvailable
                            is QueryChannelsController.ChannelsState.Result ->
                                State.Result(channelState.channels.filterNot { it.hidden == true || it.isDraft })
                        }
                    }
                ) { state -> stateMerger.value = state }
                paginationStateMerger.addSource(queryChannelsController.loadingMore) { loadingMore ->
                    setPaginationState { copy(loadingMore = loadingMore) }
                }
                paginationStateMerger.addSource(queryChannelsController.endOfChannels) { endOfChannels ->
                    setPaginationState { copy(endOfChannels = endOfChannels) }
                }
            } else {
                stateMerger.postValue(State.NoChannelsAvailable)
            }
        }
    }

    public fun onEvent(event: Event) {
        when (event) {

            is Event.ReachedEndOfList -> requestMoreChannels()
            is Event.LogoutClicked -> {
                ChatClient.instance().disconnect()
                stateMerger.postValue(State.NavigateToLoginScreen)
            }
        }.exhaustive
    }

    public fun leaveChannel(channel: Channel) {
        chatDomain.useCases.leaveChannel(channel.cid).enqueue()
    }

    public fun deleteChannel(channel: Channel) {
        chatDomain.useCases.deleteChannel(channel.cid).enqueue()
    }

    public fun hideChannel(channel: Channel) {
        chatDomain.useCases.hideChannel(channel.cid, true).enqueue()
    }

    public fun markAllRead() {
        chatDomain.useCases.markAllRead().enqueue()
    }

    private fun requestMoreChannels() {
        chatDomain.useCases.queryChannelsLoadMore(filter, sort).enqueue()
    }

    private fun setPaginationState(reducer: PaginationState.() -> PaginationState) {
        paginationStateMerger.value = reducer(paginationStateMerger.value ?: PaginationState())
    }

    public sealed class State {
        public object Loading : State()
        public data class Result(val channels: List<Channel>) : State()
        public object NoChannelsAvailable : State()
        public object NavigateToLoginScreen : State()
    }

    public data class PaginationState(
        val loadingMore: Boolean = false,
        val endOfChannels: Boolean = false
    )

    public sealed class Event {
        public object ReachedEndOfList : Event()
        public object LogoutClicked : Event()
    }

    public companion object {
        @JvmField
        public val DEFAULT_SORT: QuerySort<Channel> = QuerySort.desc("last_updated")
    }
}
