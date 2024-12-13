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

package io.getstream.chat.ui.sample.feature.chat.info.group.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.query.AddMembersParams
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.state.extensions.watchChannelAsState
import io.getstream.chat.android.state.utils.Event
import io.getstream.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

class GroupChatInfoAddUsersViewModel(
    cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: Flow<ChannelState> =
        chatClient.watchChannelAsState(cid, 0, viewModelScope).filterNotNull()

    private val channelClient = chatClient.channel(cid)
    private var members: List<Member> = emptyList()
    private val _state: MutableLiveData<State> = MutableLiveData(INITIAL_STATE)
    private val _userAddedState: MutableLiveData<Boolean> = MutableLiveData(false)
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    private var isLoadingMore: Boolean = false
    val state: LiveData<State> = _state
    val userAddedState: LiveData<Boolean> = _userAddedState
    val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    private val membersLiveData: LiveData<List<Member>> = channelState.flatMapLatest { it.members }.asLiveData()

    private val observer = Observer<List<Member>> { members = it }

    init {
        membersLiveData.observeForever(observer)
        viewModelScope.launch {
            fetchUsers()
        }
    }

    override fun onCleared() {
        membersLiveData.removeObserver(observer)
        super.onCleared()
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.UserClicked -> addMember(action.user)
            is Action.SearchQueryChanged -> setQuery(action.query)
            Action.LoadMoreRequested -> loadMore()
        }
    }

    private fun addMember(user: User) {
        viewModelScope.launch {
            val message = Message(text = "${user.name} was added to this channel")
            val params = AddMembersParams(
                members = listOf(MemberData(user.id)),
                systemMessage = message,
            )
            when (channelClient.addMembers(params).await()) {
                is Result.Success -> _userAddedState.value = true
                is Result.Failure -> _errorEvents.postValue(Event(ErrorEvent.AddMemberError))
            }
        }
    }

    private fun loadMore() {
        viewModelScope.launch {
            val currentState = _state.value!!

            if (!currentState.canLoadMore || isLoadingMore) {
                return@launch
            }

            isLoadingMore = true
            fetchUsers()
        }
    }

    private fun setQuery(query: String) {
        viewModelScope.launch {
            _state.value = State(
                query = query,
                results = emptyList(),
                isLoading = true,
                canLoadMore = true,
            )
            fetchUsers()
        }
    }

    private suspend fun fetchUsers() {
        if (members.isEmpty()) {
            return
        }
        val currentState = _state.value!!
        val filter = if (currentState.query.isEmpty()) {
            Filters.neutral()
        } else {
            Filters.autocomplete("name", currentState.query)
        }

        val result = ChatClient.instance().queryUsers(
            QueryUsersRequest(
                filter = filter,
                offset = currentState.results.size,
                limit = QUERY_LIMIT,
            ),
        ).await()

        when (result) {
            is Result.Success -> _state.value = currentState.copy(
                results = currentState.results + result.value,
                isLoading = false,
                canLoadMore = result.value.size == QUERY_LIMIT,
            )
            is Result.Failure -> _state.value = currentState.copy(
                isLoading = false,
                canLoadMore = true,
            )
        }
    }

    data class State(
        val query: String,
        val canLoadMore: Boolean,
        val results: List<User>,
        val isLoading: Boolean,
    )

    sealed class Action {
        data class UserClicked(val user: User) : Action()
        data class SearchQueryChanged(val query: String) : Action()
        object LoadMoreRequested : Action()
    }

    sealed class ErrorEvent {
        object AddMemberError : ErrorEvent()
    }

    companion object {
        private const val QUERY_LIMIT = 20
        private val INITIAL_STATE = State(query = "", canLoadMore = true, results = emptyList(), isLoading = true)
    }
}
