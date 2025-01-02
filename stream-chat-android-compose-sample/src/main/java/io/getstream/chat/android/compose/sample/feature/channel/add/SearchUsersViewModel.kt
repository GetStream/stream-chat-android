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

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Error
import io.getstream.result.call.Call
import io.getstream.result.call.enqueue
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel managing the 'Search users' logic.
 *
 * @param chatClient The [ChatClient] instance used to interact with the Stream Chat SDK.
 */
open class SearchUsersViewModel(
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Exposes the [SearchUsersState] of the screen to be rendered.
     */
    private val _searchUsersState: MutableStateFlow<SearchUsersState> = MutableStateFlow(initialState())
    val searchUsersState: StateFlow<SearchUsersState>
        get() = _searchUsersState

    /**
     * Exposes the error events to be observed by the UI.
     */
    private val _searchUsersError: MutableSharedFlow<SearchError> = MutableSharedFlow(extraBufferCapacity = 1)
    val searchUsersError: MutableSharedFlow<SearchError>
        get() = _searchUsersError

    // Pagination data
    private var offset: Int = 0
    private var isEndReached: Boolean = false

    // Call state management
    private var searchCall: Call<List<User>>? = null

    init {
        searchUsers()
    }

    /**
     * Invoked when the search query has changed.
     *
     * @param query The new search query.
     */
    fun onSearchQueryChanged(query: String) {
        // Ignore same query
        if (query == _searchUsersState.value.query) return
        // Update pagination data
        this.offset = 0
        this.isEndReached = false
        // Search users with new query
        _searchUsersState.update {
            it.copy(query = query, users = emptyMap(), isLoading = true)
        }
        searchUsers()
    }

    /**
     * Invoked when a user is clicked.
     * It selects/unselects the user to be added/removed from the new channel.
     *
     * @param user The clicked user.
     */
    fun onUserClick(user: User) {
        val updatedUsers = if (_searchUsersState.value.selectedUsers.contains(user)) {
            _searchUsersState.value.selectedUsers - user
        } else {
            _searchUsersState.value.selectedUsers + user
        }
        _searchUsersState.update { it.copy(selectedUsers = updatedUsers) }
    }

    /**
     * Invoked when the the end of the users list has been reached.
     * Loads the next page of users.
     */
    fun onEndOfListReached() {
        // Prevent loading of more users if all users are already loaded
        if (isEndReached || _searchUsersState.value.isLoading) {
            return
        }
        _searchUsersState.update { it.copy(isLoading = true) }
        searchUsers()
    }

    private fun searchUsers() {
        searchCall?.cancel()
        searchCall = chatClient.queryUsers(searchQuery(_searchUsersState.value.query))
        searchCall?.enqueue(
            onSuccess = { users ->
                // Append new results
                val currentUsers = _searchUsersState.value.users
                val allUsers = currentUsers.flatMap { it.value } + users
                val groupedUsers = groupUsers(allUsers)
                _searchUsersState.update {
                    it.copy(users = groupedUsers, isLoading = false)
                }
                // Update pagination data
                this.offset += users.size
                this.isEndReached = users.size < SEARCH_USERS_LIMIT
            },
            onError = { error ->
                _searchUsersState.update { it.copy(isLoading = false) }
                _searchUsersError.tryEmit(SearchError(error))
            },
        )
    }

    private fun searchQuery(query: String): QueryUsersRequest {
        // Exclude the current user from the search results
        val currentUserId = chatClient.clientState.user.value?.id
        val currentUserFilter = if (currentUserId != null) {
            Filters.ne(SEARCH_USERS_FIELD_ID, currentUserId)
        } else {
            null
        }
        // Filter users by name
        val nameFilter = if (query.isEmpty()) {
            Filters.neutral()
        } else {
            Filters.autocomplete(SEARCH_USERS_FIELD_NAME, query)
        }
        val filter = if (currentUserFilter != null) {
            Filters.and(nameFilter, currentUserFilter)
        } else {
            nameFilter
        }
        return QueryUsersRequest(
            filter = filter,
            offset = offset,
            limit = SEARCH_USERS_LIMIT,
            querySort = SEARCH_USERS_SORT,
            presence = true,
        )
    }

    private fun groupUsers(users: List<User>): Map<Char, List<User>> {
        return users
            .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: EMPTY_NAME_SYMBOL }
            .toSortedMap()
    }

    private fun initialState() = SearchUsersState(
        query = "",
        users = emptyMap(),
        selectedUsers = emptyList(),
        isLoading = true,
    )

    companion object {
        private const val SEARCH_USERS_LIMIT = 30
        private const val SEARCH_USERS_FIELD_NAME = "name"
        private const val SEARCH_USERS_FIELD_ID = "id"
        private val SEARCH_USERS_SORT = QuerySortByField.ascByName<User>(SEARCH_USERS_FIELD_NAME)

        /**
         * Placeholder symbol for users with empty names.
         */
        const val EMPTY_NAME_SYMBOL = Char.MAX_VALUE
    }

    /**
     * Defines the state of the search users screen.
     *
     * @param query The current search query.
     * @param users Map of search user results grouped by the first letter of their name.
     * @param selectedUsers List of users selected to be added to the new channel.
     * @param isLoading True if the screen is loading, false otherwise.
     */
    data class SearchUsersState(
        val query: String,
        val users: Map<Char, List<User>>,
        val selectedUsers: List<User>,
        val isLoading: Boolean,
    )

    /**
     * Defines the error event that can be emitted by the ViewModel.
     *
     * @param error The error that occurred.
     */
    data class SearchError(val error: Error)
}
