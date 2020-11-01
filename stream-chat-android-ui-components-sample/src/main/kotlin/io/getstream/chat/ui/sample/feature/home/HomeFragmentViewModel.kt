package io.getstream.chat.ui.sample.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User

class HomeFragmentViewModel : ViewModel() {

    private val _state: MutableLiveData<State> = MutableLiveData()

    val state: LiveData<State>
        get() = _state

    init {
        _state.value = State.Result(
            user = ChatClient.instance().getCurrentUser() ?: User(),
            totalUnreadCount = 27,
            mentionsUnreadCount = 5
        )
    }

    fun onEvent(action: UiAction) {
        when (action) {
            is UiAction.LogoutClicked -> {
                ChatClient.instance().disconnect()
                _state.postValue(State.NavigateToLoginScreen)
            }
        }
    }

    sealed class State {
        data class Result(
            val user: User,
            val totalUnreadCount: Int,
            val mentionsUnreadCount: Int
        ) : State()

        object NavigateToLoginScreen : State()
    }

    sealed class UiAction {
        object LogoutClicked : UiAction()
    }
}
