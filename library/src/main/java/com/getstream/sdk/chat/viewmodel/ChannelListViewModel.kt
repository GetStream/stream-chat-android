package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.Chat
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel.Companion.DEFAULT_FILTER
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel.Companion.DEFAULT_PAGE_SIZE
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel.Companion.DEFAULT_SORT
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

interface ChannelsViewModel {
    val state: LiveData<State>

    fun onAction(action: Action)

    sealed class State {
        data class LoadingMore(val isLoading: Boolean) : State()
        data class Loading(val isLoading: Boolean) : State()
        data class Result(val channels: List<Channel>) : State()
        data class EndPage(val isEndPage: Boolean) : State()
        data class RedirectToChannel(val channel: Channel) : State()
    }

    sealed class Action {
        data class QueryChannels(val filter: FilterObject = DEFAULT_FILTER,
                                 val sort: QuerySort = DEFAULT_SORT) : Action()

        object LoadMore : Action()
        data class UpdatePageSize(val filter: FilterObject, val sort: QuerySort) : Action()
        data class CreateNewChannel(val data: Nothing) : Action()
        data class ChannelClick(val channel: Channel) : Action()
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 10
        val DEFAULT_FILTER: FilterObject = eq("type", "messaging")
        val DEFAULT_SORT: QuerySort = QuerySort().desc("last_message_at")
    }
}

class ChannelsViewModelImpl(
        private val chatDomain: ChatDomain = ChatDomain.instance(),
        private val filter: FilterObject = DEFAULT_FILTER,
        private val sort: QuerySort = DEFAULT_SORT,
        private var pageSize: Int = DEFAULT_PAGE_SIZE
) : ChannelsViewModel, ViewModel() {

    private val channelsData: LiveData<ChannelsViewModel.State.Result>
    private val loadingMoreData: LiveData<ChannelsViewModel.State.LoadingMore>
    private val loadingData: LiveData<ChannelsViewModel.State.Loading>
    private val endPageData: LiveData<ChannelsViewModel.State.EndPage>
    private val stateMerger = MediatorLiveData<ChannelsViewModel.State>()

    override val state: LiveData<ChannelsViewModel.State> = stateMerger

    init {
        val queryChannelsController = chatDomain.useCases.queryChannels(filter, sort).execute().data()

        queryChannelsController.run {
            channelsData = map(channels) { ChannelsViewModel.State.Result(it) }
            loadingMoreData = map(loadingMore) { ChannelsViewModel.State.LoadingMore(it) }
            loadingData = map(loading) { ChannelsViewModel.State.Loading(it) }
            endPageData = map(endOfChannels) { ChannelsViewModel.State.EndPage(it) }
        }

        stateMerger.addSource(channelsData) { state -> stateMerger.value = state }
        stateMerger.addSource(loadingMoreData) { state -> stateMerger.value = state }
        stateMerger.addSource(loadingData) { state -> stateMerger.value = state }
    }

    override fun onAction(action: ChannelsViewModel.Action) {
        when (action) {
            is ChannelsViewModel.Action.QueryChannels -> chatDomain.useCases.queryChannelsLoadMore(filter, sort)
            is ChannelsViewModel.Action.LoadMore -> loadPreviousPage()

        }
    }

    private fun loadPreviousPage() {
        TODO()
    }
}