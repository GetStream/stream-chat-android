package com.getstream.sdk.chat.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.viewmodel.ChannelsViewModel.Companion.DEFAULT_FILTER
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
        data class LoadingNextPage(val isLoading: Boolean) : State()
        data class Loading(val isLoading: Boolean) : State()
        data class Result(val channels: List<Channel>) : State()
        data class EndPageReached(val isEndPage: Boolean) : State()
    }

    sealed class Action {
        object ReachedEndOfList : Action()
    }

    companion object {
        val DEFAULT_FILTER: FilterObject = eq("type", "messaging")
        val DEFAULT_SORT: QuerySort = QuerySort().desc("last_message_at")
    }
}

class ChannelsViewModelImpl(
        private val chatDomain: ChatDomain = ChatDomain.instance(),
        private val filter: FilterObject = DEFAULT_FILTER,
        private val sort: QuerySort = DEFAULT_SORT
) : ChannelsViewModel, ViewModel() {
    private val channelsData: LiveData<ChannelsViewModel.State.Result>
    private val loadingMoreData: LiveData<ChannelsViewModel.State.LoadingNextPage>
    private val loadingData: LiveData<ChannelsViewModel.State.Loading>
    private val endPageData: LiveData<ChannelsViewModel.State.EndPageReached>
    private val stateMerger = MediatorLiveData<ChannelsViewModel.State>()

    override val state: LiveData<ChannelsViewModel.State> = stateMerger

    init {
        Log.d("ChannelsViewModel", "init")
        val queryChannelsController = chatDomain.useCases.queryChannels(filter, sort).execute().data()
        queryChannelsController.run {
            channelsData = map(channels) { ChannelsViewModel.State.Result(it) }
            loadingMoreData = map(loadingMore) { ChannelsViewModel.State.LoadingNextPage(it) }
            loadingData = map(loading) { ChannelsViewModel.State.Loading(it) }
            endPageData = map(endOfChannels) { ChannelsViewModel.State.EndPageReached(it) }
        }

        stateMerger.addSource(loadingData) { state -> stateMerger.value = state }
        stateMerger.addSource(channelsData) { state -> stateMerger.value = state }
        stateMerger.addSource(loadingMoreData) { state -> stateMerger.value = state }
    }

    override fun onAction(action: ChannelsViewModel.Action) {
        when (action) {
            is ChannelsViewModel.Action.ReachedEndOfList -> requestMoreChannels()
        }
    }

    private fun requestMoreChannels() {
        chatDomain.useCases.queryChannelsLoadMore(filter, sort)
    }
}