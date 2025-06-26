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

package io.getstream.chat.android.compose.sample.ui.channel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.query.AddMembersParams
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.state.extensions.watchChannelAsState
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

class AddMembersViewModel(
    private val cid: String,
    private val resultLimit: Int,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val channelMembers = chatClient
        .watchChannelAsState(cid, messageLimit = 0, viewModelScope)
        .filterNotNull()
        .flatMapLatest { it.members }

    private val _state = MutableStateFlow(AddMembersViewState(currentUser = chatClient.getCurrentUser()))
    val state: StateFlow<AddMembersViewState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AddMembersViewEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<AddMembersViewEvent> = _events.asSharedFlow()

    init {
        _state
            .map { it.query }
            .debounce(TypingDebounceTimeoutInMillis)
            .distinctUntilChanged()
            .onEach { query ->
                _state.update { currentState ->
                    currentState.copy(
                        isLoading = true,
                    )
                }
                val channelMemberIds = (channelMembers.firstOrNull() ?: emptyList()).map(Member::getUserId)
                chatClient.queryUsers(query.toRequest())
                    .await()
                    .onSuccess { users ->
                        val searchResult = users.filterNot { it.id in channelMemberIds }
                        _state.update { currentState ->
                            currentState.copy(
                                isLoading = false,
                                searchResult = searchResult,
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
    }

    fun onViewAction(viewAction: AddMembersViewAction) {
        when (viewAction) {
            is AddMembersViewAction.QueryChanged -> {
                _state.update { currentState ->
                    currentState.copy(
                        query = viewAction.query.trim(),
                    )
                }
            }

            is AddMembersViewAction.UserClick -> {
                _state.update { currentState ->
                    val user = viewAction.user
                    val isSelected = currentState.selectedUsers.contains(user)
                    val newSelectedUsers = if (isSelected) {
                        currentState.selectedUsers - user
                    } else {
                        currentState.selectedUsers + user
                    }
                    currentState.copy(
                        selectedUsers = newSelectedUsers,
                    )
                }
            }

            AddMembersViewAction.ConfirmClick -> {
                _state.update { currentState ->
                    currentState.copy(isLoading = true)
                }
                viewModelScope.launch {
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
}

private const val TypingDebounceTimeoutInMillis = 300L

data class AddMembersViewState(
    val currentUser: User? = null,
    val isLoading: Boolean = true,
    val query: String = "",
    val searchResult: List<User> = emptyList(),
    val selectedUsers: List<User> = emptyList(),
)

sealed class AddMembersViewAction {
    data class QueryChanged(val query: String) : AddMembersViewAction()
    data class UserClick(val user: User) : AddMembersViewAction()
    data object ConfirmClick : AddMembersViewAction()
}

sealed class AddMembersViewEvent {
    data object MembersAdded : AddMembersViewEvent()
}

class AddMembersViewModelFactory(
    private val cid: String,
    private val resultLimit: Int = 6,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AddMembersViewModel::class.java) {
            "AddMembersViewModelFactory can only create instances of AddMembersViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return AddMembersViewModel(cid, resultLimit, chatClient) as T
    }
}
