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
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Filters.eq
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.ChatDomain

interface ChannelsViewModel {
    val state: LiveData<State>

    fun onEvent(event: Event)

    sealed class State {
        data class LoadingNextPage(val isLoading: Boolean) : State()
        object Loading : State()
        data class Result(val channels: List<Channel>) : State()
        data class EndPageReached(val isEndPage: Boolean) : State()
        object NoChannelsAvailable : State()
        object NavigateToLoginScreen : State()
    }

    sealed class Event {
        object ReachedEndOfList : Event()
        object LogoutClicked : Event()
    }

    companion object {
        @JvmField
        val DEFAULT_SORT: QuerySort = QuerySort().desc("last_updated")
    }
}

class ChannelsViewModelImpl(
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val filter: FilterObject = Filters.and(eq("type", "messaging"), Filters.`in`("members", listOf(chatDomain.currentUser.id))),
    private val sort: QuerySort = DEFAULT_SORT
) : ChannelsViewModel, ViewModel() {
    private val channelsData: LiveData<ChannelsViewModel.State>
    private val loadingMoreData: LiveData<ChannelsViewModel.State.LoadingNextPage>
    private val loadingData = MutableLiveData<ChannelsViewModel.State.Loading>()
    private val endPageData: LiveData<ChannelsViewModel.State.EndPageReached>
    private val stateMerger = MediatorLiveData<ChannelsViewModel.State>()

    private val logger = ChatLogger.get("ChannelsViewModelImpl")

    override val state: LiveData<ChannelsViewModel.State> = stateMerger

    init {
        val queryChannelsController = chatDomain.useCases.queryChannels(filter, sort).execute().data()
        queryChannelsController.run {
            loadingData.postValue(ChannelsViewModel.State.Loading)
            channelsData = map(channels) {
                val messages = it.map { it.messages.lastOrNull()?.text }
                logger.logI("last message text observing, channelsData found ${messages}")
                if (it.isEmpty()) {
                    ChannelsViewModel.State.NoChannelsAvailable
                } else {
                    ChannelsViewModel.State.Result(it)
                }
            }
            loadingMoreData = map(loadingMore) { ChannelsViewModel.State.LoadingNextPage(it) }
            endPageData = map(endOfChannels) { ChannelsViewModel.State.EndPageReached(it) }
        }
        queryChannelsController.channels.observeForever {
            val messages = it.map { it.messages.lastOrNull()?.text }
            logger.logI("last message text observing, found $messages")
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
        chatDomain.useCases.queryChannelsLoadMore(filter, sort).enqueue { Unit }
    }
}
