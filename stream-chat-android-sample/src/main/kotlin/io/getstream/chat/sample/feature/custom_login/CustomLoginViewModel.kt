package io.getstream.chat.sample.feature.custom_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.sample.application.App
import io.getstream.chat.sample.application.FirebaseLogger
import io.getstream.chat.sample.common.name
import io.getstream.chat.sample.data.user.SampleUser
import timber.log.Timber
import io.getstream.chat.android.client.models.User as ChatUser

class CustomLoginViewModel : ViewModel() {
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

    /**
     * You would normally initialize the Chat SDK only once in the Application class,
     * but since we allow changing API keys at runtime in this demo app, we have to
     * reinitialize the Chat SDK here with the new API key.
     */
    private fun initChatSdk(credentials: LoginCredentials) {
        App.instance.chatInitializer.init(credentials.apiKey)
    }

    private fun initChatUser(loginCredentials: LoginCredentials) {
        val chatUser = ChatUser().apply {
            id = loginCredentials.userId
            name = loginCredentials.userName
        }
        App.instance.userRepository.user = SampleUser(
            id = loginCredentials.userId,
            name = loginCredentials.userName,
            token = loginCredentials.userToken,
            image = "https://getstream.io/random_png?id=${loginCredentials.userId}&name=${loginCredentials.userName}&size=200"
        )
        ChatClient.instance()
            .connectUser(
                chatUser,
                loginCredentials.userToken
            ).enqueue { result ->
                if (result.isSuccess) {
                    _state.postValue(State.RedirectToChannels)
                    Timber.d("User set successfully")
                    FirebaseLogger.userId = result.data().user.id
                } else {
                    _state.postValue(State.Error(result.error().message))
                    Timber.e("Failed to set user ${result.error()}")
                }
            }
    }

    private fun getInvalidFields(credentials: LoginCredentials): List<ValidatedField> {
        return ArrayList<ValidatedField>().apply {
            if (credentials.apiKey.isEmpty()) {
                add(ValidatedField.API_KEY)
            }
            if (credentials.userId.isEmpty()) {
                add(ValidatedField.USER_ID)
            }
            if (credentials.userToken.isEmpty()) {
                add(ValidatedField.USER_TOKEN)
            }
        }
    }
}

sealed class State {
    object RedirectToChannels : State()
    object Loading : State()
    data class Error(val errorMessage: String?) : State()
    data class ValidationError(val invalidFields: List<ValidatedField>) : State()
}

data class LoginCredentials(
    val apiKey: String,
    val userId: String,
    val userToken: String,
    val userName: String,
)

enum class ValidatedField {
    API_KEY,
    USER_ID,
    USER_TOKEN
}
