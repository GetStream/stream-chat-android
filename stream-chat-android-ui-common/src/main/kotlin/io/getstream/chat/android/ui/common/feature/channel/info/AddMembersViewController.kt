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

package io.getstream.chat.android.ui.common.feature.channel.info

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.api.state.watchChannelAsState
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.query.AddMembersParams
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.common.state.channel.info.AddMembersViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Controller responsible for managing the state and events related to adding members to a channel.
 *
 * It provides functionality to search for users, select them, and add them as members to the channel.
 *
 * @param cid The unique identifier of the channel.
 * @param scope The [CoroutineScope] used for launching coroutines.
 * @param resultLimit The maximum number of search results to return.
 * @param chatClient The [ChatClient] instance used for interacting with the chat API.
 * @param channelState A [Flow] representing the state of the channel.
 */
@InternalStreamChatApi
public class AddMembersViewController(
    private val cid: String,
    private val scope: CoroutineScope,
    private val resultLimit: Int = DEFAULT_RESULT_LIMIT,
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState> = chatClient
        .watchChannelAsState(cid = cid, messageLimit = 0, coroutineScope = scope)
        .filterNotNull(),
) {

    private val channelMembers = channelState.flatMapLatest { it.members }

    private val _state = MutableStateFlow(AddMembersViewState())

    /**
     * A [StateFlow] representing the current state of the "Add Members" view.
     */
    public val state: StateFlow<AddMembersViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddMembersViewEvent>(extraBufferCapacity = 1)

    /**
     * A [SharedFlow] that emits one-shot events related to the "Add Members" view.
     */
    public val events: SharedFlow<AddMembersViewEvent> = _events.asSharedFlow()

    init {
        _state
            .map { it.query }
            .debounce(TYPING_DEBOUNCE_TIMEOUT_MS)
            .distinctUntilChanged()
            .onEach { query ->
                _state.update { it.copy(isLoading = true) }
                val channelMemberIds = (channelMembers.firstOrNull() ?: emptyList())
                    .map(Member::getUserId)
                chatClient.queryUsers(query.toRequest())
                    .await()
                    .onSuccess { users ->
                        val searchResult = users.filterNot { it.id in channelMemberIds }
                        _state.update { it.copy(isLoading = false, searchResult = searchResult) }
                    }
            }
            .launchIn(scope)
    }

    /**
     * Handles actions dispatched from the "Add Members" view.
     *
     * @param action The [AddMembersViewAction] to handle.
     */
    public fun onViewAction(action: AddMembersViewAction) {
        when (action) {
            is AddMembersViewAction.QueryChanged -> {
                _state.update { it.copy(query = action.query.trim()) }
            }

            is AddMembersViewAction.UserClick -> {
                _state.update { currentState ->
                    val user = action.user
                    val isSelected = currentState.selectedUsers.contains(user)
                    val newSelectedUsers = if (isSelected) {
                        currentState.selectedUsers - user
                    } else {
                        currentState.selectedUsers + user
                    }
                    currentState.copy(selectedUsers = newSelectedUsers)
                }
            }

            is AddMembersViewAction.ConfirmClick -> {
                _state.update { it.copy(isLoading = true) }
                scope.launch {
                    val params = AddMembersParams(
                        members = _state.value.selectedUsers.map { user -> MemberData(user.id) },
                        systemMessage = null,
                    )
                    chatClient
                        .channel(cid)
                        .addMembers(params)
                        .await()
                        .onSuccess { _events.tryEmit(AddMembersViewEvent.MembersAdded) }
                }
            }
        }
    }

    private fun String.toRequest(): QueryUsersRequest {
        val filter = if (isEmpty()) {
            Filters.neutral()
        } else {
            Filters.autocomplete("name", this)
        }
        return QueryUsersRequest(
            filter = filter,
            offset = 0,
            limit = resultLimit,
            querySort = QuerySortByField.ascByName("name"),
            presence = true,
        )
    }

    private companion object {
        private const val DEFAULT_RESULT_LIMIT = 30
        private const val TYPING_DEBOUNCE_TIMEOUT_MS = 300L
    }
}
