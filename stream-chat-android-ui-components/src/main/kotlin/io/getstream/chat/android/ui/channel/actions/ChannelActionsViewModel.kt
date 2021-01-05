package io.getstream.chat.android.ui.channel.actions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.utils.extensions.isCurrentUser
import io.getstream.chat.android.ui.utils.extensions.isOwnerOrAdmin
import kotlinx.coroutines.launch

internal class ChannelActionsViewModel(
    cid: String,
    private val isGroup: Boolean,
    chatDomain: ChatDomain = ChatDomain.instance(),
) : ViewModel() {

    private val initialState = State()
    private var currentState = initialState
    private val _state = MutableLiveData<State>()
    val state: LiveData<State> = Transformations.distinctUntilChanged(_state)

    init {
        _state.postValue(currentState)

        viewModelScope.launch {
            chatDomain
                .useCases
                .watchChannel(cid, 0)
                .await()
                .data()
                .let { channelController ->
                    channelController.members.observeForever { members ->
                        onAction(Action.UpdateMembers(members))
                    }
                }
        }
    }

    data class State(
        val members: List<Member> = listOf(),
        val canDeleteChannel: Boolean = false,
    )

    sealed class Action {
        data class UpdateMembers(val members: List<Member>) : Action()
    }

    fun onAction(action: Action) {
        currentState = reduce(action)
        _state.postValue(currentState)
    }

    private fun reduce(action: Action): State {
        return when (action) {
            is Action.UpdateMembers -> updateMembers(action.members)
        }
    }

    private fun updateMembers(members: List<Member>): State {
        val canDeleteChannel = members.firstOrNull { it.user.isCurrentUser() }?.isOwnerOrAdmin ?: false
        return currentState.copy(
            members = members.filter { isGroup || !it.user.isCurrentUser() },
            canDeleteChannel = canDeleteChannel,
        )
    }
}
