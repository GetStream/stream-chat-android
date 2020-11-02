package io.getstream.chat.ui.sample.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.ui.sample.common.SingleLiveEvent

class HomeFragmentViewModel : ViewModel() {

    private val chatDomain: ChatDomain = ChatDomain.instance()
    private val _state: MediatorLiveData<State> = MediatorLiveData()
    private val _events: SingleLiveEvent<UiEvent> = SingleLiveEvent()

    val state: LiveData<State> = _state
    val events: LiveData<UiEvent> = _events

    init {
        _state.value = State(
            user = ChatClient.instance().getCurrentUser() ?: User(),
        )

        val totalUnreadCount = chatDomain.useCases
            .getTotalUnreadCount()
            .execute()
            .data()
        _state.addSource(totalUnreadCount) { totalUnreadCount ->
            setState { copy(totalUnreadCount = totalUnreadCount) }
        }
    }

    private fun setState(reducer: State.() -> State) {
        _state.value = reducer(_state.value ?: State())
    }

    fun onUiAction(action: UiAction) {
        when (action) {
            is UiAction.LogoutClicked -> {
                ChatClient.instance().disconnect()
                _events.value = UiEvent.NavigateToLoginScreen
            }
        }
    }

    data class State(
        val user: User = User(),
        val totalUnreadCount: Int = 0,
        // TODO: implement unread mentions count
        val mentionsUnreadCount: Int = 5
    )

    sealed class UiAction {
        object LogoutClicked : UiAction()
    }

    sealed class UiEvent {
        object NavigateToLoginScreen : UiEvent()
    }
}
