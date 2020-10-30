package io.getstream.chat.ui.sample.feature.user_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.application.AppConfig
import io.getstream.chat.ui.sample.common.image
import io.getstream.chat.ui.sample.common.name
import io.getstream.chat.ui.sample.data.user.User
import timber.log.Timber
import io.getstream.chat.android.client.models.User as ChatUser

class UserLoginViewModel : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        _state.postValue(State.AvailableUsers(AppConfig.availableUsers))
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
        App.instance.chatInitializer.init(AppConfig.apiKey)
    }

    private fun initChatUser(user: User, cid: String? = null) {
        App.instance.userRepository.user = user
        val chatUser = ChatUser().apply {
            id = user.id
            image = user.image
            name = user.name
        }
        ChatClient.instance().setUser(
            chatUser,
            user.token,
            object : InitConnectionListener() {
                override fun onSuccess(data: ConnectionData) {
                    if (cid != null) {
                        _state.postValue(State.RedirectToChannel(cid))
                    } else {
                        _state.postValue(State.RedirectToChannels)
                    }
                    Timber.d("User set successfully")
                }

                override fun onError(error: ChatError) {
                    _state.postValue(State.Error(error.message))
                    Timber.e("Failed to set user $error")
                }
            }
        )
    }

    fun targetChannelDataReceived(cid: String) {
        val user = App.instance.userRepository.user
        if (user != User.None) {
            initChatUser(user, cid)
        }
    }
}

sealed class State {
    data class AvailableUsers(val availableUsers: List<User>) : State()
    object RedirectToChannels : State()
    data class RedirectToChannel(val cid: String) : State()
    object Loading : State()
    data class Error(val errorMessage: String?) : State()
}
