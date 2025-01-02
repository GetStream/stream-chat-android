/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.feature.channel.add

import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.compose.sample.feature.channel.CHANNEL_ARG_DRAFT
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.User
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel managing the "Add Channel" flow.
 *
 * @param chatClient The [ChatClient] instance used to interact with the Stream Chat SDK.
 */
class AddChannelViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : SearchUsersViewModel(chatClient) {

    private val draftCid = MutableStateFlow<String?>(null)

    /**
     * Exposes the [AddChannelState] of the screen to be rendered.
     */
    val state = combine(searchUsersState, draftCid, ::AddChannelState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT),
            initialValue = AddChannelState(searchUsersState.value, draftCid.value),
        )

    /**
     * Exposes the navigation events to be observed by the UI.
     */
    private val _navigationEvent: MutableSharedFlow<NavigationEvent> = MutableSharedFlow()
    val navigationEvent: SharedFlow<NavigationEvent>
        get() = _navigationEvent

    /**
     * Exposes error events to be observed by the UI.
     */
    private val _errorEvent: MutableSharedFlow<ErrorEvent> = MutableSharedFlow(extraBufferCapacity = 1)
    val errorEvent: SharedFlow<ErrorEvent>
        get() = _errorEvent

    init {
        // Create a draft channel whenever the selected users change
        viewModelScope.launch {
            searchUsersState
                .map { it.selectedUsers }
                .distinctUntilChanged()
                .collectLatest {
                    createDraftChannel(it)
                }
        }
        // Propagate search users errors to the UI.
        viewModelScope.launch {
            searchUsersError.collectLatest { error ->
                _errorEvent.emit(ErrorEvent.SearchUsersError(error))
            }
        }
    }

    /**
     * Invoked when a message is sent to the draft channel.
     * Creates the channel (no longer draft) and navigates to it.
     */
    fun onMessageSent() {
        createChannel()
    }

    /**
     * Creates a draft channel with the given [members].
     */
    private fun createDraftChannel(members: List<User>) {
        if (members.isEmpty()) {
            draftCid.value = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val userId = chatClient.clientState.user.value?.id ?: error("User must be set before create new channel!")
            val params = CreateChannelParams(
                members = (members.map(User::id) + userId).map(::MemberData),
                extraData = mapOf(CHANNEL_ARG_DRAFT to true),
            )
            val result = chatClient
                .createChannel(channelType = CHANNEL_TYPE_MESSAGING, channelId = "", params = params)
                .execute()
            when (result) {
                is Result.Success -> {
                    val cid = result.value.cid
                    draftCid.value = cid
                }

                is Result.Failure -> {
                    _errorEvent.tryEmit(ErrorEvent.CreateDraftChannelError(result.value))
                }
            }
        }
    }

    private fun createChannel() {
        val cid = draftCid.value ?: return
        val client = chatClient.channel(cid)
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = client.update(extraData = mapOf(CHANNEL_ARG_DRAFT to false)).execute()) {
                is Result.Success -> {
                    // Open the newly created channel
                    _navigationEvent.emit(NavigationEvent.NavigateToChannel(result.value.cid))
                }

                is Result.Failure -> {
                    _errorEvent.tryEmit(ErrorEvent.CreateChannelError(result.value))
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_TYPE_MESSAGING = "messaging"

        private const val STOP_TIMEOUT = 5000L
    }

    /**
     * Defines the UI state of the "Add Channel" screen.
     *
     * @param searchUsersState The search users state.
     * @param draftCid The CID of the draft channel (if there is one created).
     */
    data class AddChannelState(
        val searchUsersState: SearchUsersState,
        val draftCid: String? = null,
    )

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
         * Represents an error event when creating a draft channel.
         */
        data class CreateDraftChannelError(val error: Error) : ErrorEvent

        /**
         * Represents an error event when creating a channel from draft.
         */
        data class CreateChannelError(val error: Error) : ErrorEvent
    }
}
