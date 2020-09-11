package io.getstream.chat.sample.feature.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.Chat
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.sample.application.AppConfig
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.User
import io.getstream.chat.sample.data.user.UserRepository
import timber.log.Timber

typealias ChatUser = io.getstream.chat.android.client.models.User

class LoginViewModel(
    private val appConfig: AppConfig,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        _state.postValue(State.AvailableUsers(appConfig.availableUsers))
    }

    fun userClicked(user: User) {
        userRepository.user = user
        _state.postValue(State.Progress)
        Chat.getInstance().setUser(
            user.toChatUser(), user.token,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    Timber.d("User set successfully")
                    _state.postValue(State.LoggedIn)
                }

                override fun onError(error: ChatError) {
                    Timber.e("Failed to set user")
                    _state.postValue(State.FailedToLogin(appConfig.availableUsers))
                }
            }
        )
    }

    private fun User.toChatUser(): ChatUser = ChatUser().apply {
        id = this@toChatUser.id
        image = this@toChatUser.image
        name = this@toChatUser.name
    }
}

sealed class State {
    data class AvailableUsers(val availableUsers: List<User>) : State()
    data class FailedToLogin(val availableUsers: List<User>) : State()
    object Progress : State()
    object LoggedIn : State()
}
