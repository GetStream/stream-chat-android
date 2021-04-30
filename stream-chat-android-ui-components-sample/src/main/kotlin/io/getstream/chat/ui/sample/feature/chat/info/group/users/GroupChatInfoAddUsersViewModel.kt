package io.getstream.chat.ui.sample.feature.chat.info.group.users

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryUsersRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import kotlinx.coroutines.launch

class GroupChatInfoAddUsersViewModel(
    cid: String,
    chatDomain: ChatDomain = ChatDomain.instance(),
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val channelClient = chatClient.channel(cid)
    private var members: List<Member> = emptyList()
    private val _state: MutableLiveData<State> = MutableLiveData(INITIAL_STATE)
    private val _userAddedState: MutableLiveData<Boolean> = MutableLiveData(false)
    private var isLoadingMore: Boolean = false
    val state: LiveData<State> = _state
    val userAddedState: LiveData<Boolean> = _userAddedState
    private var channelController: ChannelController? = null

    private val observer = Observer<List<Member>> { members = it }

    init {
        viewModelScope.launch {
            val result = chatDomain.getChannelController(cid).await()
            if (result.isSuccess) {
                channelController = result.data()
                channelController?.members?.observeForever(observer)
                viewModelScope.launch {
                    fetchUsers()
                }
            }
        }
    }

    override fun onCleared() {
        channelController?.members?.removeObserver(observer)
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
            val response = channelClient.addMembers(user.id).await()
            if (response.isSuccess) {
                _userAddedState.value = true
            } else {
                // TODO: Handle error
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
        val currentMembers = members
        val currentState = _state.value!!
        val filter = if (currentState.query.isEmpty()) {
            Filters.nin("id", currentMembers.map { it.getUserId() })
        } else {
            Filters.and(
                Filters.autocomplete("name", currentState.query),
                Filters.nin("id", currentMembers.map { it.getUserId() })
            )
        }

        val result = ChatClient.instance().queryUsers(
            QueryUsersRequest(
                filter = filter,
                offset = currentState.results.size,
                limit = QUERY_LIMIT,
            )
        ).await()

        if (result.isSuccess) {
            _state.value = currentState.copy(
                results = currentState.results + result.data(),
                isLoading = false,
                canLoadMore = result.data().size == QUERY_LIMIT
            )
        } else {
            _state.value = currentState.copy(
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

    companion object {
        private const val QUERY_LIMIT = 20
        private val INITIAL_STATE = State(query = "", canLoadMore = true, results = emptyList(), isLoading = true)
    }
}
