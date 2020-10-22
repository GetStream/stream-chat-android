package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.utils.exhaustive
import com.getstream.sdk.chat.viewmodel.channels.ChannelsViewModel.Companion.DEFAULT_SORT
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

public interface ChannelsViewModel {
    public val state: LiveData<State>

    public fun onEvent(event: Event)

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
        public val DEFAULT_SORT: QuerySort = QuerySort().desc("last_updated")
    }
}

public class ChannelsViewModelImpl(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject = Filters.and(eq("type", "messaging"), Filters.`in`("members", listOf(chatDomain.currentUser.id))),
    private val sort: QuerySort = DEFAULT_SORT
) : ChannelsViewModel, ViewModel() {
    private val channelsData: LiveData<ChannelsViewModel.State>
    private val loadingMoreData: LiveData<ChannelsViewModel.State.LoadingNextPage>
    private val loadingData = MutableLiveData<ChannelsViewModel.State.Loading>()
    private val endPageData: LiveData<ChannelsViewModel.State.EndPageReached>
    private val stateMerger = MediatorLiveData<ChannelsViewModel.State>()

    override val state: LiveData<ChannelsViewModel.State> = stateMerger

    init {
        val queryChannelsController = chatDomain.useCases.queryChannels(filter, sort).execute().data()
        queryChannelsController.run {
            loadingData.postValue(ChannelsViewModel.State.Loading)
            channelsData = map(channels) {
                if (it.isEmpty()) {
                    ChannelsViewModel.State.NoChannelsAvailable
                } else {
                    ChannelsViewModel.State.Result(it)
                }
            }
            loadingMoreData = map(loadingMore) { ChannelsViewModel.State.LoadingNextPage(it) }
            endPageData = map(endOfChannels) { ChannelsViewModel.State.EndPageReached(it) }
        }

        stateMerger.addSource(loadingData) { state -> stateMerger.value = state }
        stateMerger.addSource(channelsData) { state -> stateMerger.value = state }
        stateMerger.addSource(loadingMoreData) { state -> stateMerger.value = state }
    }

    override fun onEvent(event: ChannelsViewModel.Event) {
        when (event) {
            is ChannelsViewModel.Event.ReachedEndOfList -> requestMoreChannels()
            is ChannelsViewModel.Event.LogoutClicked -> {
                Chat.getInstance().disconnect()
                stateMerger.postValue(ChannelsViewModel.State.NavigateToLoginScreen)
            }
        }.exhaustive
    }

    private fun requestMoreChannels() {
        chatDomain.useCases.queryChannelsLoadMore(filter, sort).enqueue()
    }

    override fun onCleared() {
        super.onCleared()
    }
}
