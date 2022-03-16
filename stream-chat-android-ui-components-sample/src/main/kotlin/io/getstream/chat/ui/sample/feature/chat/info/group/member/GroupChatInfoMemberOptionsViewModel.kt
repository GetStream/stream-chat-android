package io.getstream.chat.ui.sample.feature.chat.info.group.member

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@OptIn(InternalStreamChatApi::class)
class GroupChatInfoMemberOptionsViewModel(
    private val cid: String,
    private val memberId: String,
    private val chatClient: ChatClient = ChatClient.instance(),
    private val globalState: GlobalState = chatClient.globalState
) : ViewModel() {

    private val _events = MutableLiveData<Event<UiEvent>>()
    private val _state: MediatorLiveData<State> = MediatorLiveData()
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    val events: LiveData<Event<UiEvent>> = _events
    val state: LiveData<State> = _state
    val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    init {
        viewModelScope.launch {
            globalState.user
                .filterNotNull()
                .map { user ->
                    chatClient.queryChannels(
                        request = QueryChannelsRequest(
                            filter = Filters.and(
                                Filters.eq("type", "messaging"),
                                Filters.distinct(listOf(memberId, user.id)),
                            ),
                            querySort = QuerySort.desc(Channel::lastUpdated),
                            messageLimit = 0,
                            limit = 1,
                        )
                    ).await()
                }.flatMapConcat { result ->
                    if (result.isSuccess) {
                        val cid = result.data().firstOrNull()?.cid ?: ""

                        chatClient.watchChannelAsState(cid, 30, viewModelScope)
                            .messagesState
                            .map { mapChannelState(it, cid) }
                    } else {
                        MutableStateFlow(null)
                    }
                }
                .filterNotNull()
                .collect { state ->
                    _state.value = state
                }
        }
    }

    private fun mapChannelState(messagesState: MessagesState, cid: String): State {
        return when (messagesState) {
            is MessagesState.Result -> {
                State(
                    directChannelCid = cid,
                    loading = false,
                )
            }
            MessagesState.NoQueryActive,
            MessagesState.Loading,
            -> State(directChannelCid = null, loading = true)
            MessagesState.OfflineNoResults -> State(
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
            val result = chatClient.channel(cid).removeMembers(memberId).await()
            if (result.isSuccess) {
                _events.value = Event(UiEvent.Dismiss)
            } else {
                _errorEvents.postValue(Event(ErrorEvent.RemoveMemberError))
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

    sealed class ErrorEvent {
        object RemoveMemberError : ErrorEvent()
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
