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
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.ui.common.state.channel.info.AddMembersViewState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Controller responsible for managing the state and actions related to adding members to a channel.
 *
 * It provides functionality to search for users, select/deselect them, and paginate results.
 * Adding the selected members to the channel is delegated to the parent (e.g. [ChannelInfoViewController]).
 *
 * @param scope The [CoroutineScope] used for launching coroutines.
 * @param resultLimit The maximum number of search results per page.
 * @param chatClient The [ChatClient] instance used for interacting with the chat API.
 * @param channelState A [Flow] representing the live state of the channel, used to keep track of
 * current members reactively so that the list remains accurate when the sheet is re-opened.
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@InternalStreamChatApi
public class AddMembersViewController(
    private val scope: CoroutineScope,
    private val resultLimit: Int = DEFAULT_RESULT_LIMIT,
    private val chatClient: ChatClient = ChatClient.instance(),
    channelState: Flow<ChannelState>,
) {

    private val channelMembers = channelState.flatMapLatest { it.members }

    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    private val _state = MutableStateFlow(AddMembersViewState())

    /**
     * A [StateFlow] representing the current state of the "Add Members" view.
     */
    public val state: StateFlow<AddMembersViewState> = _state.asStateFlow()

    init {
        // Keep loadedMemberIds in sync with the live channel member list.
        channelMembers
            .map { members -> members.mapTo(mutableSetOf(), Member::getUserId) }
            .distinctUntilChanged()
            .onEach { memberIds -> _state.update { it.copy(loadedMemberIds = memberIds) } }
            .launchIn(scope)

        // Re-run search whenever the query changes, with debounce.
        _state
            .map { it.query }
            .debounce(TYPING_DEBOUNCE_TIMEOUT_MS)
            .distinctUntilChanged()
            .onEach { query -> searchUsers(query) }
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
                _state.update { it.copy(query = action.query) }
            }

            is AddMembersViewAction.UserClick -> {
                _state.update { currentState ->
                    val userId = action.user.id
                    val newSelectedIds = if (userId in currentState.selectedUserIds) {
                        currentState.selectedUserIds - userId
                    } else {
                        currentState.selectedUserIds + userId
                    }
                    currentState.copy(selectedUserIds = newSelectedIds)
                }
            }

            is AddMembersViewAction.LoadMore -> loadMore()
        }
    }

    private fun searchUsers(query: String) {
        searchJob?.cancel()
        loadMoreJob?.cancel()
        searchJob = scope.launch {
            _state.update { it.copy(isLoading = true) }
            chatClient.queryUsers(query.trim().toSearchRequest(offset = 0))
                .await()
                .onSuccess { users ->
                    if (!isActive) return@onSuccess
                    _state.update { it.copy(isLoading = false, searchResult = users) }
                }
                .onError {
                    if (!isActive) return@onError
                    _state.update { it.copy(isLoading = false) }
                }
        }
    }

    private fun loadMore() {
        if (_state.value.isLoading || _state.value.isLoadingMore) return
        loadMoreJob = scope.launch {
            val currentResult = _state.value.searchResult
            _state.update { it.copy(isLoadingMore = true) }
            chatClient.queryUsers(_state.value.query.trim().toSearchRequest(offset = currentResult.size))
                .await()
                .onSuccess { newUsers ->
                    _state.update {
                        val existingIds = it.searchResult.mapTo(mutableSetOf()) { user -> user.id }
                        val unique = newUsers.filterNot { user -> user.id in existingIds }
                        it.copy(isLoadingMore = false, searchResult = it.searchResult + unique)
                    }
                }
                .onError {
                    _state.update { it.copy(isLoadingMore = false) }
                }
        }
    }

    private fun String.toSearchRequest(offset: Int): QueryUsersRequest {
        val filter = if (isEmpty()) {
            Filters.neutral()
        } else {
            Filters.or(
                Filters.autocomplete("name", this),
                Filters.autocomplete("id", this),
            )
        }
        return QueryUsersRequest(
            filter = filter,
            offset = offset,
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
