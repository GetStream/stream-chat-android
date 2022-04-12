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

package io.getstream.chat.android.ui.channel.actions.internal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.ui.common.extensions.internal.isCurrentUser
import io.getstream.chat.android.ui.common.extensions.isCurrentUserOwnerOrAdmin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * Used by [ChannelActionsDialogFragment] to provide the correct state
 * containing information about the channel.
 *
 * @param isGroup True if the Channel is a group channel, false otherwise.
 * @param chatClient The main entry point for all low-level chat operations.
 */
internal class ChannelActionsViewModel(
    cid: String,
    private val isGroup: Boolean,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    val channelState: Flow<ChannelState> =
        chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = DEFAULT_MESSAGE_LIMIT,
            coroutineScope = viewModelScope
        ).filterNotNull()

    /**
     * The initial empty state.
     */
    private val initialState = State()

    /**
     * The current state containing channel information.
     */
    private var currentState = initialState

    /**
     * The state containing channel information wrapped in MutableLiveData.
     */
    private val _state = MediatorLiveData<State>()

    /**
     * The state containing channel information wrapped in LiveData.
     */
    val state: LiveData<State> = Transformations.distinctUntilChanged(_state)

    init {
        _state.postValue(currentState)

        channelState.flatMapLatest { it.members }.onEach { members ->
            onAction(Action.UpdateMembers(members))
        }.launchIn(viewModelScope)
    }

    /**
     * Processes actions and updates the state accordingly.
     *
     * @param action The action to process. Results in a state update as a side-effect.
     */
    fun onAction(action: Action) {
        currentState = reduce(action)
        _state.postValue(currentState)
    }

    /**
     * Checks against available actions and produces state accordingly.
     *
     * @param action The action to process.
     *
     * @return Returns the updated [State].
     */
    private fun reduce(action: Action): State {
        return when (action) {
            is Action.UpdateMembers -> updateMembers(action.members)
        }
    }

    /**
     * Returns the updated state containing all current members.
     * Filters out the current user if the channel is not a group
     * channel.
     *
     * @param members List of all members belonging to the channel.
     *
     * @return Returns the updated [State].
     */
    private fun updateMembers(members: List<Member>): State {
        val canDeleteChannel = members.isCurrentUserOwnerOrAdmin()
        return currentState.copy(
            members = members.filter { isGroup || !it.user.isCurrentUser() },
            canDeleteChannel = canDeleteChannel,
        )
    }

    /**
     * Holds information about the channel.
     *
     * @param members List of members belonging to the channel.
     * @param canDeleteChannel If the current user has the ability to delete the channel.
     */
    data class State(
        val members: List<Member> = listOf(),
        val canDeleteChannel: Boolean = false,
    )

    /**
     * Describes actions that are meant to be taken and result in a state
     * update.
     */
    sealed class Action {
        data class UpdateMembers(val members: List<Member>) : Action()
    }

    private companion object {

        /**
         * The default limit for messages count in requests.
         */
        private const val DEFAULT_MESSAGE_LIMIT: Int = 0
    }
}
