package io.getstream.chat.sample.feature.user_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.sample.application.App
import io.getstream.chat.sample.application.AppConfig
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.SampleUser
import timber.log.Timber
import io.getstream.chat.android.client.models.User as ChatUser

class UserLoginViewModel : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    init {
        _state.postValue(State.AvailableUsers(AppConfig.availableUsers))
    }

    fun userClicked(user: SampleUser) {
        _state.postValue(State.Loading)
        initChatUser(user)
    }

    /**
     * You would normally initialize the Chat SDK only once in the Application class,
     * but since we allow changing API keys at runtime in this demo app, we have to
     * reinitialize the Chat SDK here with the new API key.
     */
    private fun initChatSdk(user: ChatUser) {
        App.instance.chatInitializer.init(AppConfig.apiKey, user)
    }

    private fun initChatUser(user: SampleUser, cid: String? = null) {
        App.instance.userRepository.user = user
        val chatUser = ChatUser().apply {
            id = user.id
            image = user.image
            name = user.name
        }
        initChatSdk(chatUser)
        ChatClient.instance().connectUser(chatUser, user.token)
            .enqueue { result ->
                if (result.isSuccess) {
                    Timber.d("User set successfully")
                } else {
                    _state.postValue(State.Error(result.error().message))
                    Timber.e("Failed to set user ${result.error()}")
                }
            }
        if (cid != null) {
            _state.postValue(State.RedirectToChannel(cid))
        } else {
            _state.postValue(State.RedirectToChannels)
        }
    }

    fun targetChannelDataReceived(cid: String) {
        val user = App.instance.userRepository.user
        if (user != SampleUser.None) {
            initChatUser(user, cid)
        }
    }
}

sealed class State {
    data class AvailableUsers(val availableUsers: List<SampleUser>) : State()
    object RedirectToChannels : State()
    data class RedirectToChannel(val cid: String) : State()
    object Loading : State()
    data class Error(val errorMessage: String?) : State()
}
