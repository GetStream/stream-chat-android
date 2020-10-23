package io.getstream.chat.sample.feature.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.Chat
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.sample.application.ChatInitializer
import io.getstream.chat.sample.application.FirebaseLogger
import io.getstream.chat.sample.common.image
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.User
import io.getstream.chat.sample.data.user.UserRepository
import timber.log.Timber
import io.getstream.chat.android.client.models.User as ChatUser

class LoginViewModel(
    private val chatInitializer: ChatInitializer,
    private val userRepository: UserRepository
) : ViewModel() {
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = _state

    fun loginButtonClicked(credentials: LoginCredentials) {
        val invalidFields = getInvalidFields(credentials)
        if (invalidFields.isEmpty()) {
            _state.postValue(State.Loading)
            initChatSdk(credentials)
            initChatUser(credentials)
        } else {
            _state.postValue(State.ValidationError(invalidFields))
        }
    }

    fun targetChannelDataReceived(cid: String) {
        val user = userRepository.user
        if (userRepository.user != User.None) {
            val chatUser = ChatUser().apply {
                id = user.id
                image = user.image
                name = user.name
            }
            initChatUser(chatUser, user.token, cid)
        }
    }

    /**
     * You would normally initialize the Chat SDK only once in the Application class,
     * but since we allow changing API keys at runtime in this demo app, we have to
     * reinitialize the Chat SDK here with the new API key.
     */
    private fun initChatSdk(credentials: LoginCredentials) {
        chatInitializer.init(credentials.apiKey)
    }

    private fun initChatUser(loginCredentials: LoginCredentials) {
        val chatUser = ChatUser().apply {
            id = loginCredentials.userId
            name = loginCredentials.userName
        }
        initChatUser(chatUser, loginCredentials.userToken)
    }

    private fun initChatUser(chatUser: ChatUser, token: String, cid: String? = null) {
        Chat.getInstance()
            .setUser(
                chatUser,
                token,
                object : InitConnectionListener() {
                    override fun onSuccess(data: ConnectionData) {
                        if (cid != null) {
                            _state.postValue(State.RedirectToChannel(cid))
                        } else {
                            _state.postValue(State.RedirectToChannels)
                        }
                        Timber.d("User set successfully")
                        FirebaseLogger.userId = data.user.id
                    }

                    override fun onError(error: ChatError) {
                        _state.postValue(State.Error(error.message))
                        Timber.e("Failed to set user $error")
                    }
                }
            )
    }

    private fun getInvalidFields(credentials: LoginCredentials): List<ValidatedField> {
        return ArrayList<ValidatedField>().apply {
            if (credentials.apiKey.isNullOrEmpty()) {
                add(ValidatedField.API_KEY)
            }
            if (credentials.userId.isNullOrEmpty()) {
                add(ValidatedField.USER_ID)
            }
            if (credentials.userToken.isNullOrEmpty()) {
                add(ValidatedField.USER_TOKEN)
            }
        }
    }
}

sealed class State {
    object RedirectToChannels : State()
    object Loading : State()
    data class RedirectToChannel(val cid: String) : State()
    data class Error(val errorMessage: String?) : State()
    data class ValidationError(val invalidFields: List<ValidatedField>) : State()
}

data class LoginCredentials(
    val apiKey: String,
    val userId: String,
    val userToken: String,
    val userName: String
)

enum class ValidatedField {
    API_KEY,
    USER_ID,
    USER_TOKEN
}
