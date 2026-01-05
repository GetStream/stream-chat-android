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

package io.getstream.chat.ui.sample.feature.userlogin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.Event
import io.getstream.chat.android.models.ConnectionData
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.application.AppConfig
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import io.getstream.chat.android.models.User as ChatUser

class UserLoginViewModel : ViewModel() {
    private val logger by taggedLogger("Chat:UserLoginViewModel")
    private val _state = MutableLiveData<State>()
    private val _events = MutableLiveData<Event<UiEvent>>()

    val state: LiveData<State> = _state
    val events: LiveData<Event<UiEvent>> = _events

    private var switchUser: Boolean = false

    fun init(switchUser: Boolean) {
        this.switchUser = switchUser

        val user = App.instance.userRepository.getUser()
        if (user != SampleUser.None && !switchUser) {
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
        if (switchUser) {
            App.instance.userRepository.clearUser()
        }

        App.instance.userRepository.setUser(user)

        val chatUser = ChatUser(
            id = user.id,
            image = user.image,
            name = user.name,
            language = user.language,
            privacySettings = user.privacySettings,
        )

        ChatClient.instance().run {
            if (switchUser) {
                switchUser(chatUser, user.token) {
                    _events.postValue(Event(UiEvent.RedirectToChannels))
                }.enqueue(::handleUserConnection)
            } else {
                viewModelScope.launch {
                    clientState.initializationState
                        .transformWhile {
                            emit(it)
                            it != InitializationState.COMPLETE
                        }
                        .collect {
                            when (it) {
                                InitializationState.COMPLETE -> _events.postValue(Event(UiEvent.RedirectToChannels))
                                InitializationState.INITIALIZING -> { }
                                InitializationState.NOT_INITIALIZED -> {
                                    launch { connectUser(chatUser, user.token).await().let(::handleUserConnection) }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun handleUserConnection(result: Result<ConnectionData>) {
        when (result) {
            is Result.Success -> logger.d { "User set successfully" }
            is Result.Failure -> {
                _events.postValue(Event(UiEvent.Error(result.value.message)))
                logger.d { "Failed to set user ${result.value}" }
            }
        }
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

    internal companion object {
        internal const val EXTRA_SWITCH_USER = "EXTRA_SWITCH_USER"
    }
}
