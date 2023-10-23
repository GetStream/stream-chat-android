/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.ui.sample.feature.user_login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.ConnectionData
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.ui.sample.application.App
import io.getstream.chat.ui.sample.application.AppConfig
import io.getstream.chat.ui.sample.data.user.SampleUser
import io.getstream.logging.StreamLog
import java.util.Locale
import io.getstream.chat.android.client.models.User as ChatUser

class UserLoginViewModel : ViewModel() {
    private val logger = StreamLog.getLogger("Chat:UserLoginViewModel")
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

        val chatUser = ChatUser().apply {
            id = user.id
            image = user.image
            name = user.name
            language = Locale.getDefault().language
        }

        ChatClient.instance().run {
            if (switchUser) {
                switchUser(chatUser, user.token) {
                    _events.postValue(Event(UiEvent.RedirectToChannels))
                }.enqueue(::handleUserConnection)
            } else {
                if (getCurrentUser() == null) {
                    connectUser(chatUser, user.token).enqueue(::handleUserConnection)
                }

                _events.postValue(Event(UiEvent.RedirectToChannels))
            }
        }
    }

    private fun handleUserConnection(result: Result<ConnectionData>) {
        if (result.isSuccess) {
            logger.d { "User set successfully" }
        } else {
            _events.postValue(Event(UiEvent.Error(result.error().message)))
            logger.d { "Failed to set user ${result.error()}" }
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
