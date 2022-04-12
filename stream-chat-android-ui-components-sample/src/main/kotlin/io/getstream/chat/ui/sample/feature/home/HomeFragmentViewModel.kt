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

package io.getstream.chat.ui.sample.feature.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.extensions.globalState
import io.getstream.chat.ui.sample.application.App

class HomeFragmentViewModel : ViewModel() {

    private val _state: MediatorLiveData<State> = MediatorLiveData()
    private val _events: MutableLiveData<Event<UiEvent>> = MutableLiveData()

    val state: LiveData<State> = _state
    val events: LiveData<Event<UiEvent>> = _events

    init {
        _state.value = State(
            user = ChatClient.instance().getCurrentUser() ?: unauthorizedUser,
        )

        val totalUnreadCount = ChatClient.instance().globalState.totalUnreadCount.asLiveData()
        _state.addSource(totalUnreadCount) { count ->
            setState { copy(totalUnreadCount = count) }
        }
    }

    private fun setState(reducer: State.() -> State) {
        _state.value = reducer(_state.value ?: State())
    }

    fun onUiAction(action: UiAction) {
        when (action) {
            is UiAction.LogoutClicked -> {
                ChatClient.instance().disconnect()
                App.instance.userRepository.clearUser()
                _events.value = Event(UiEvent.NavigateToLoginScreen)
            }
        }
    }

    data class State(
        val user: User = User(),
        val totalUnreadCount: Int = 0,
        // TODO: implement unread mentions count
        val mentionsUnreadCount: Int = 0,
    )

    sealed class UiAction {
        object LogoutClicked : UiAction()
    }

    sealed class UiEvent {
        object NavigateToLoginScreen : UiEvent()
    }

    companion object {
        val unauthorizedUser = User()
    }
}
