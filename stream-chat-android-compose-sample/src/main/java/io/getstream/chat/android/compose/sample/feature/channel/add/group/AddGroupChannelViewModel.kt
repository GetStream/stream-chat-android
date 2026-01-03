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

package io.getstream.chat.android.compose.sample.feature.channel.add.group

import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.compose.sample.feature.channel.add.SearchUsersViewModel
import io.getstream.chat.android.models.MemberData
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * ViewModel class manging the 'Add group channel' screen.
 *
 * @param chatClient The instance of the ChatClient used to search users / create channel.
 */
class AddGroupChannelViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : SearchUsersViewModel(chatClient) {

    private val step = MutableStateFlow(AddGroupChannelStep.SELECT_USERS)
    private val channelName = MutableStateFlow("")

    /**
     * Exposes the state of the 'Add group channel' screen to be rendered.
     */
    val state = combine(step, searchUsersState, channelName, ::AddGroupChannelState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT),
            initialValue = AddGroupChannelState(step.value, searchUsersState.value, channelName.value),
        )

    /**
     * Exposes the navigation events to be observed by the UI.
     */
    private val _navigationEvent = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    val navigationEvent: SharedFlow<NavigationEvent>
        get() = _navigationEvent

    /**
     * Exposes error events to be observed by the UI.
     */
    private val _errorEvent = MutableSharedFlow<ErrorEvent>(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<ErrorEvent>
        get() = _errorEvent

    init {
        // Propagate search users errors to the UI.
        viewModelScope.launch {
            searchUsersError.collectLatest { error ->
                _errorEvent.emit(ErrorEvent.SearchUsersError(error))
            }
        }
    }

    /**
     * Invoked when the user taps on the 'Next' button.
     */
    fun onNext() {
        // Go to next step if the current step is 'SELECT_USERS'.
        if (step.value == AddGroupChannelStep.SELECT_USERS) {
            step.value = AddGroupChannelStep.ENTER_NAME
        }
    }

    /**
     * Invoked when the user taps on the 'Back' button.
     */
    fun onBack() {
        when (step.value) {
            AddGroupChannelStep.SELECT_USERS -> {
                _navigationEvent.tryEmit(NavigationEvent.Close)
            }

            AddGroupChannelStep.ENTER_NAME -> {
                step.value = AddGroupChannelStep.SELECT_USERS
            }
        }
    }

    /**
     * Invoked when the entered channel name has changed.
     *
     * @param channelName The new channel name.
     */
    fun onChannelNameChanged(channelName: String) {
        this.channelName.value = channelName
    }

    /**
     * Invoked when the user taps on the 'Create channel' button.
     */
    fun onCreateChannelClick() {
        if (channelName.value.isEmpty()) return
        if (state.value.searchUsersState.selectedUsers.isEmpty()) return
        createChannel()
    }

    private fun createChannel() {
        val currentUserId =
            chatClient.clientState.user.value?.id ?: error("User must be set before creating a new channel!")
        val name = channelName.value
        val memberIds = state.value.searchUsersState.selectedUsers.map { it.id } + currentUserId
        val params = CreateChannelParams(
            members = memberIds.map(::MemberData),
            extraData = mapOf(EXTRA_DATA_CHANNEL_NAME to name),
        )
        viewModelScope.launch {
            val result = chatClient.createChannel(
                channelType = CHANNEL_TYPE_MESSAGING,
                channelId = UUID.randomUUID().toString(),
                params = params,
            ).await()
            when (result) {
                is Result.Success -> {
                    _navigationEvent.emit(NavigationEvent.NavigateToChannel(result.value.cid))
                }

                is Result.Failure -> {
                    _errorEvent.emit(ErrorEvent.CreateChannelError(result.value))
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_TYPE_MESSAGING = "messaging"
        private const val EXTRA_DATA_CHANNEL_NAME = "name"

        private const val STOP_TIMEOUT = 5000L
    }

    /**
     * Defines the state of the 'Add group channel' screen.
     *
     * @param step The current active step of the flow.
     * @param searchUsersState The state of the search users screen.
     * @param channelName The name of the channel to be created.
     */
    data class AddGroupChannelState(
        val step: AddGroupChannelStep = AddGroupChannelStep.SELECT_USERS,
        val searchUsersState: SearchUsersState,
        val channelName: String,
    )

    /**
     * Defines the steps of the 'Add group channel' flow.
     */
    enum class AddGroupChannelStep {
        /**
         * Represents the users selection step of the flow.
         */
        SELECT_USERS,

        /**
         * Represents the channel name input step of the flow.
         */
        ENTER_NAME,
    }

    /**
     * Defines the possible navigation events that can be triggered by the ViewModel.
     */
    sealed interface NavigationEvent {

        /**
         * Navigates to the channel with the given [cid].
         *
         * @param cid The ID of the channel to navigate to.
         */
        data class NavigateToChannel(val cid: String) : NavigationEvent

        /**
         * Closes the flow.
         */
        data object Close : NavigationEvent
    }

    /**
     * Defines the possible error events that can be triggered by the ViewModel.
     */
    sealed interface ErrorEvent {

        /**
         * Represents an error which occurred during the search of users.
         */
        data class SearchUsersError(val error: SearchError) : ErrorEvent

        /**
         * Represents an error which occurred during the creation of new the channel.
         */
        data class CreateChannelError(val error: Error) : ErrorEvent
    }
}
