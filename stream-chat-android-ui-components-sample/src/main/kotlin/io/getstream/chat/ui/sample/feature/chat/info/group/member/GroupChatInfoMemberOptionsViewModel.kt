package io.getstream.chat.ui.sample.feature.chat.info.group.member

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.ui.sample.common.isDraft
import kotlinx.coroutines.launch

class GroupChatInfoMemberOptionsViewModel(
    private val cid: String,
    private val memberId: String,
    private val chatDomain: ChatDomain = ChatDomain.instance(),
) : ViewModel() {

    private val _events = MutableLiveData<Event<UiEvent>>()
    private val _state: MediatorLiveData<State> = MediatorLiveData()
    val events: LiveData<Event<UiEvent>> = _events
    val state: LiveData<State> = _state

    init {
        viewModelScope.launch {
            val result = chatDomain.queryChannels(
                filter = Filters.and(
                    Filters.eq("type", "messaging"),
                    Filters.distinct(listOf(memberId, chatDomain.currentUser.id)),
                ),
                sort = QuerySort.desc(Channel::lastUpdated),
                messageLimit = 0,
                limit = 1,
            ).await()
            if (result.isSuccess) {
                _state.addSource(
                    Transformations.map(
                        result.data().channelsState,
                        this@GroupChatInfoMemberOptionsViewModel::mapChannelState,
                    )
                ) { newState -> _state.value = newState }
            }
        }
    }

    private fun mapChannelState(channelState: QueryChannelsController.ChannelsState): State {
        return when (channelState) {
            is QueryChannelsController.ChannelsState.Result -> {
                State(
                    directChannelCid = channelState.channels.filterNot { channel ->
                        channel.isDraft
                    }.firstOrNull()?.cid,
                    loading = false,
                )
            }
            QueryChannelsController.ChannelsState.NoQueryActive,
            QueryChannelsController.ChannelsState.Loading,
            -> State(directChannelCid = null, loading = true)
            QueryChannelsController.ChannelsState.OfflineNoResults -> State(
                directChannelCid = null,
                loading = false,
            )
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.MessageClicked -> handleMessageClicked()
            Action.RemoveFromChannel -> removeFromChannel()
        }
    }

    private fun handleMessageClicked() {
        val state = state.value!!
        _events.value = Event(
            if (state.directChannelCid != null) {
                UiEvent.RedirectToChat(state.directChannelCid)
            } else {
                UiEvent.RedirectToChatPreview
            },
        )
    }

    private fun removeFromChannel() {
        viewModelScope.launch {
            val result = chatDomain.removeMembers(cid, memberId).await()
            if (result.isSuccess) {
                _events.value = Event(UiEvent.Dismiss)
            } else {
                // TODO: Handle error
            }
        }
    }

    data class State(val directChannelCid: String?, val loading: Boolean)

    sealed class Action {
        object MessageClicked : Action()
        object RemoveFromChannel : Action()
    }

    sealed class UiEvent {
        object Dismiss : UiEvent()
        data class RedirectToChat(val cid: String) : UiEvent()
        object RedirectToChatPreview : UiEvent()
    }
}

class GroupChatInfoMemberOptionsViewModelFactory(private val cid: String, private val memberId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == GroupChatInfoMemberOptionsViewModel::class.java) {
            "GroupChatInfoMemberOptionsViewModelFactory can only create instances of GroupChatInfoMemberOptionsViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return GroupChatInfoMemberOptionsViewModel(cid, memberId) as T
    }
}
