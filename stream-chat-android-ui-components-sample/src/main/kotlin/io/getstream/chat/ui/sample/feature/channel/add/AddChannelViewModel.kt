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

package io.getstream.chat.ui.sample.feature.channel.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.ui.sample.common.CHANNEL_ARG_DRAFT
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import io.getstream.chat.android.state.utils.Event as EventWrapper

class AddChannelViewModel : ViewModel() {

    private val chatClient = ChatClient.instance()
    private val _state: MutableLiveData<State> = MutableLiveData()
    private val _paginationState: MutableLiveData<PaginationState> = MutableLiveData()
    private val _errorEvents: MutableLiveData<EventWrapper<ErrorEvent>> = MutableLiveData()
    val state: LiveData<State> = _state
    val paginationState: LiveData<PaginationState> = _paginationState
    val errorEvents: LiveData<EventWrapper<ErrorEvent>> = _errorEvents

    private var channelClient: ChannelClient? = null
    private var searchQuery: String = ""
    private var offset: Int = 0
    private var latestSearchCall: Call<List<User>>? = null

    init {
        requestUsers(isRequestingMore = false)
    }

    fun onEvent(event: Event) {
        when (event) {
            Event.ReachedEndOfList -> requestMoreUsers()
            Event.MessageSent -> createChannel()
            is Event.MembersChanged -> createDraftChannel(event.members)
            is Event.SearchInputChanged -> searchUsers(event.query)
        }
    }

    private fun requestUsers(isRequestingMore: Boolean) {
        if (!isRequestingMore) {
            _state.value = State.Loading
        }
        latestSearchCall?.cancel()
        latestSearchCall = chatClient.queryUsers(createSearchQuery(searchQuery, offset, USERS_LIMIT, true))
        latestSearchCall?.enqueue { result ->
            if (result is Result.Success) {
                val users = result.value
                _state.postValue(if (isRequestingMore) State.ResultMoreUsers(users) else State.Result(users))
                updatePaginationData(users)
            }
        }
    }

    private fun createSearchQuery(
        querySearch: String,
        offset: Int,
        usersLimit: Int,
        userPresence: Boolean,
    ): QueryUsersRequest {
        val filter = if (querySearch.isEmpty()) {
            val currentUserId = chatClient.clientState.user.value?.id
            if (currentUserId != null) {
                Filters.ne(FIELD_ID, currentUserId)
            } else {
                Filters.neutral()
            }
        } else {
            createFilter(
                Filters.autocomplete(FIELD_NAME, querySearch),
                chatClient.clientState.user.value?.id?.let { id -> Filters.ne(FIELD_ID, id) },
            )
        }
        return QueryUsersRequest(
            filter = filter,
            offset = offset,
            limit = usersLimit,
            querySort = USERS_QUERY_SORT,
            presence = userPresence,
        )
    }

    private fun createFilter(defaultFilter: FilterObject, optionalFilter: FilterObject?): FilterObject {
        return if (optionalFilter != null) {
            Filters.and(defaultFilter, optionalFilter)
        } else {
            defaultFilter
        }
    }

    private fun createChannel() {
        val client = requireNotNull(channelClient) { "Cannot create Channel without initializing ChannelClient" }
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = client.update(message = null, extraData = mapOf(CHANNEL_ARG_DRAFT to false)).await()) {
                is Result.Success -> _state.postValue(State.NavigateToChannel(result.value.cid))
                is Result.Failure -> _errorEvents.postValue(EventWrapper(ErrorEvent.CreateChannelError))
            }
        }
    }

    private fun createDraftChannel(members: List<User>) {
        if (members.isEmpty()) {
            channelClient = null
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val currentUserId =
                chatClient.clientState.user.value?.id ?: error("User must be set before create new channel!")
            val params = CreateChannelParams(
                members = (members.map(User::id) + currentUserId).map(::MemberData),
                extraData = mapOf(CHANNEL_ARG_DRAFT to true),
            )
            val result = chatClient.createChannel(
                channelType = CHANNEL_MESSAGING_TYPE,
                channelId = "",
                params = params,
            ).await()
            if (result is Result.Success) {
                val cid = result.value.cid
                channelClient = ChatClient.instance().channel(cid)
                _state.postValue(State.InitializeChannel(cid))
            }
        }
    }

    private fun searchUsers(query: String) {
        offset = 0
        searchQuery = query
        requestUsers(isRequestingMore = false)
    }

    private fun requestMoreUsers() {
        _paginationState.value = _paginationState.value?.copy(loadingMore = true) ?: PaginationState(loadingMore = true)
        requestUsers(isRequestingMore = true)
    }

    private fun updatePaginationData(result: List<User>) {
        offset += result.size
        _paginationState.postValue(PaginationState(loadingMore = false, endReached = result.size < USERS_LIMIT))
    }

    companion object {
        private const val USERS_LIMIT = 30
        private const val CHANNEL_MESSAGING_TYPE = "messaging"

        private val USERS_QUERY_SORT = QuerySortByField.ascByName<User>("name")

        private const val FIELD_NAME = "name"
        private const val FIELD_ID = "id"
    }

    sealed class State {
        object Loading : State()
        data class InitializeChannel(val cid: String) : State()
        data class Result(val users: List<User>) : State()
        data class ResultMoreUsers(val users: List<User>) : State()
        data class NavigateToChannel(val cid: String) : State()
    }

    sealed class Event {
        object ReachedEndOfList : Event()
        object MessageSent : Event()
        data class MembersChanged(val members: List<User>) : Event()
        data class SearchInputChanged(val query: String) : Event()
    }

    sealed class ErrorEvent {
        object CreateChannelError : ErrorEvent()
    }

    data class PaginationState(
        val loadingMore: Boolean = false,
        val endReached: Boolean = false,
    )
}
