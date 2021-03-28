package io.getstream.chat.ui.sample.feature.user_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.application.AppConfig
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.chat.android.client.models.User as ChatUser

class UserLoginViewModel : ViewModel() {
    private val logger = ChatLogger.get("UserLoginViewModel")
    private val _state = MutableLiveData<State>()
    private val _events = MutableLiveData<Event<UiEvent>>()

    val state: LiveData<State> = _state
    val events: LiveData<Event<UiEvent>> = _events

    fun init() {
        val user = App.instance.userRepository.getUser()
        if (user != SampleUser.None) {
            authenticateUser(user)
        } else {
            _state.postValue(State.AvailableUsers(AppConfig.availableUsers))
        }
    }

    fun onUiAction(action: UiAction) {
        when (action) {
            is UiAction.UserClicked -> authenticateUser(action.user)
            is UiAction.ComponentBrowserClicked -> _events.postValue(Event(UiEvent.RedirectToComponentBrowser))
        }
    }

    private fun authenticateUser(user: SampleUser) {
        App.instance.userRepository.setUser(user)
        val chatUser = ChatUser().apply {
            id = user.id
            image = user.image
            name = user.name
        }
        ChatClient.instance().connectUser(chatUser, user.token)
            .enqueue { result ->
                if (result.isSuccess) {
                    logger.logD("User set successfully")
                } else {
                    _events.postValue(Event(UiEvent.Error(result.error().message)))
                    logger.logD("Failed to set user ${result.error()}")
                }
            }
        _events.postValue(Event(UiEvent.RedirectToChannels))
    }

    sealed class State {
        data class AvailableUsers(val availableUsers: List<SampleUser>) : State()
    }

    sealed class UiAction {
        data class UserClicked(val user: SampleUser) : UiAction()
        object ComponentBrowserClicked : UiAction()
    }

    sealed class UiEvent {
        object RedirectToChannels : UiEvent()
        object RedirectToComponentBrowser : UiEvent()
        data class Error(val errorMessage: String?) : UiEvent()
    }
}
