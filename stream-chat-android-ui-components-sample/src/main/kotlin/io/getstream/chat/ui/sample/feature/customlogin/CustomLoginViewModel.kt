/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.ui.sample.feature.customlogin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.application.FirebaseLogger
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.chat.android.models.User as ChatUser

class CustomLoginViewModel : ViewModel() {
    private val logger by taggedLogger("Chat:CustomLoginViewModel")
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
        val chatUser = ChatUser(
            id = loginCredentials.userId,
            name = loginCredentials.userName,
        )

        ChatClient.instance().connectUser(chatUser, loginCredentials.userToken)
            .enqueue { result ->
                when (result) {
                    is Result.Success -> {
                        _state.postValue(State.RedirectToChannels)
                        logger.d { "User set successfully" }
                        FirebaseLogger.userId = result.value.user.id

                        App.instance.userRepository.setUser(
                            SampleUser(
                                apiKey = loginCredentials.apiKey,
                                id = loginCredentials.userId,
                                name = loginCredentials.userName,
                                token = loginCredentials.userToken,
                                image = "https://getstream.io/random_png?id=${loginCredentials.userId}&name=${loginCredentials.userName}&size=200",
                            ),
                        )
                    }
                    is Result.Failure -> {
                        _state.postValue(State.Error(result.value.message))
                        logger.d { "Failed to set user ${result.value}" }
                    }
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
    USER_TOKEN,
}
