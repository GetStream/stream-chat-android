package io.getstream.chat.sample.feature.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.sample.application.AppConfig
import io.getstream.chat.sample.data.user.User
import io.getstream.chat.sample.data.user.UserRepository

class LoginViewModel(
    appConfig: AppConfig,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        _state.postValue(State.AvailableUsers(appConfig.availableUsers))
    }

    fun userClicked(user: User) {
        userRepository.user = user
        _state.postValue(State.LoggedIn)
    }
}

sealed class State {
    data class AvailableUsers(val availableUsers: List<User>) : State()
    object LoggedIn : State()
}
