package io.getstream.chat.sample.feature.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.Chat
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.sample.application.AppConfig
import io.getstream.chat.sample.application.ChatInitializer
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.User
import io.getstream.chat.sample.data.user.UserRepository
import timber.log.Timber
import io.getstream.chat.android.client.models.User as ChatUser

class UsersViewModel(
    private val appConfig: AppConfig,
    private val chatInitializer: ChatInitializer,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        _state.postValue(State.AvailableUsers(appConfig.availableUsers))
    }

    fun userClicked(user: User) {
        _state.postValue(State.Loading)
        initChatSdk()
        initChatUser(user)
    }

    /**
     * You would normally initialize the Chat SDK only once in the Application class,
     * but since we allow changing API keys at runtime in this demo app, we have to
     * reinitialize the Chat SDK here with the new API key.
     */
    private fun initChatSdk() {
        chatInitializer.init(appConfig.apiKey)
    }

    private fun initChatUser(user: User) {
        userRepository.user = user
        val chatUser = ChatUser().apply {
            id = user.id
            image = user.image
            name = user.name
        }
        Chat.instance().setUser(
            chatUser,
            user.token,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    _state.postValue(State.RedirectToChannels)
                    Timber.d("User set successfully")
                }

                override fun onError(error: ChatError) {
                    _state.postValue(State.Error(error.message))
                    Timber.e("Failed to set user $error")
                }
            }
        )
    }
}

sealed class State {
    data class AvailableUsers(val availableUsers: List<User>) : State()
    object RedirectToChannels : State()
    object Loading : State()
    data class Error(val errorMessage: String?) : State()
}
