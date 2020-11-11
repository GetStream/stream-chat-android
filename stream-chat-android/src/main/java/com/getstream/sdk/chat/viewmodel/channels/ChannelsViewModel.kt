package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.utils.exhaustive
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

/***
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
    private val filter: FilterObject = Filters.and(eq("type", "messaging"), Filters.`in`("members", listOf(chatDomain.currentUser.id))),
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30
) : ViewModel() {
    private val channelsData: LiveData<State>
    private val loadingData = MutableLiveData<State.Loading>()
    private val stateMerger = MediatorLiveData<State>()
    public val state: LiveData<State> = stateMerger
    public val typingEvents: LiveData<Pair<String, List<User>>>
        get() = chatDomain.typingUpdates

    private val paginationStateMerger = MediatorLiveData<PaginationState>()
    public val paginationState: LiveData<PaginationState> = paginationStateMerger

    init {
        val queryChannelsController = chatDomain.useCases.queryChannels(filter, sort, limit).execute().data()
        queryChannelsController.run {
            loadingData.postValue(State.Loading)
            channelsData = map(channels) { channelList ->
                if (channelList.isEmpty()) {
                    State.NoChannelsAvailable
                } else {
                    State.Result(channelList.filter { it.hidden == false })
                }
            }

            paginationStateMerger.addSource(loadingMore) { loadingMore ->
                setPaginationState { copy(loadingMore = loadingMore) }
            }
            paginationStateMerger.addSource(endOfChannels) { endOfChannels ->
                setPaginationState { copy(endOfChannels = endOfChannels) }
            }
        }

        stateMerger.addSource(loadingData) { state -> stateMerger.value = state }
        stateMerger.addSource(channelsData) { state -> stateMerger.value = state }
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

    public fun hideChannel(channel: Channel) {
        loadingData.postValue(State.Loading)
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
