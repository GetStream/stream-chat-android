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
import androidx.lifecycle.distinctUntilChanged
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.globalState
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.utils.Event
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel.PaginationState
import io.getstream.chat.ui.sample.application.App
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for handling the state of bottom navigation bar and navigation
 * drawer on the home screen.
 *
 * @param chatClient  The main entry point for all low-level operations.
 * @param clientState The client state used to obtain the current user.
 * @param globalState The global state of OfflinePlugin.
 */
class HomeViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
    private val clientState: ClientState = chatClient.clientState,
    private val globalState: GlobalState = chatClient.globalState,
) : ViewModel() {

    /**
     * The initial empty state of the screen.
     */
    private val initialState = UiState()

    /**
     * The home screen state wrapped in MutableLiveData.
     */
    private val _state: MediatorLiveData<UiState> = MediatorLiveData()

    /**
     * The home screen state wrapped in LiveData.
     */
    val state: LiveData<UiState> = _state.distinctUntilChanged()

    /**
     * Emits one-shot events that should be handled only once.
     */
    private val _events: MutableLiveData<Event<UiEvent>> = MutableLiveData()

    /**
     * Emits one-shot events that should be handled only once.
     */
    val events: LiveData<Event<UiEvent>> = _events

    init {
        setState { initialState }

        _state.addSource(globalState.totalUnreadCount.asLiveData()) { count ->
            setState { copy(totalUnreadCount = count) }
        }
        _state.addSource(globalState.unreadThreadsCount.asLiveData()) { count ->
            setState { copy(unreadThreadsCount = count) }
        }
        _state.addSource(clientState.user.asLiveData()) { user ->
            setState { copy(user = user ?: User()) }
        }
    }

    /**
     * Processes actions and updates the state accordingly.
     *
     * @param action The action to process. Results in a state update as a side-effect.
     */
    fun onUiAction(action: UiAction) {
        when (action) {
            is UiAction.LogoutClicked -> {
                viewModelScope.launch {
                    chatClient.disconnect(flushPersistence = true).await()
                    App.instance.userRepository.clearUser()
                    _events.value = Event(UiEvent.NavigateToLoginScreenLogout)
                }
            }

            is UiAction.SwitchUserClicked -> {
                _events.value = Event(UiEvent.NavigateToLoginScreenSwitchUser)
            }
        }
    }

    /**
     * Sets the current home screen state.
     *
     * @param reducer A lambda function that returns [PaginationState].
     */
    private fun setState(reducer: UiState.() -> UiState) {
        _state.value = reducer(_state.value ?: UiState())
    }

    /**
     * Holds information about the state of the home screen.
     *
     * @param user The currently logged in user.
     * @param totalUnreadCount The total unread messages count for the current user.
     * @param mentionsUnreadCount The number of unread mentions by the current user.
     * @param unreadThreadsCount The number of unread threads by the current user.
     */
    data class UiState(
        val user: User = User(),
        val totalUnreadCount: Int = 0,
        val mentionsUnreadCount: Int = 0,
        val unreadThreadsCount: Int = 0,
    )

    /**
     * Describes actions that are meant to be taken and result in a state
     * update.
     */
    sealed class UiAction {
        /**
         * A click on logout button in navigation drawer.
         */
        object LogoutClicked : UiAction()
        object SwitchUserClicked : UiAction()
    }

    /**
     * Describes one-shot events that should be handled only once.
     */
    sealed class UiEvent {
        /**
         * An event to redirect the user to login screen.
         */
        object NavigateToLoginScreenLogout : UiEvent()
        object NavigateToLoginScreenSwitchUser : UiEvent()
    }
}
