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
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

public class ChannelsViewModel(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject = Filters.and(eq("type", "messaging"), Filters.`in`("members", listOf(chatDomain.currentUser.id))),
    private val sort: QuerySort<Channel> = DEFAULT_SORT,
    private val limit: Int = 30
) : ViewModel() {
    private val channelsData: LiveData<State>
    private val loadingMoreData: LiveData<State.LoadingNextPage>
    private val loadingData = MutableLiveData<State.Loading>()
    private val endPageData: LiveData<State.EndPageReached>
    private val stateMerger = MediatorLiveData<State>()

    public val state: LiveData<State> = stateMerger

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
            loadingMoreData = map(loadingMore) { State.LoadingNextPage(it) }
            endPageData = map(endOfChannels) { State.EndPageReached(it) }
        }

        stateMerger.addSource(loadingData) { state -> stateMerger.value = state }
        stateMerger.addSource(channelsData) { state -> stateMerger.value = state }
        stateMerger.addSource(loadingMoreData) { state -> stateMerger.value = state }
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

    private fun requestMoreChannels() {
        chatDomain.useCases.queryChannelsLoadMore(filter, sort).enqueue()
    }

    public sealed class State {
        public data class LoadingNextPage(val isLoading: Boolean) : State()
        public object Loading : State()
        public data class Result(val channels: List<Channel>) : State()
        public data class EndPageReached(val isEndPage: Boolean) : State()
        public object NoChannelsAvailable : State()
        public object NavigateToLoginScreen : State()
    }

    public sealed class Event {
        public object ReachedEndOfList : Event()
        public object LogoutClicked : Event()
    }

    public companion object {
        @JvmField
        public val DEFAULT_SORT: QuerySort<Channel> = QuerySort.desc("last_updated")
    }
}
