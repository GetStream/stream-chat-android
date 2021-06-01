package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.ui.common.extensions.isCurrentUserOwnerOrAdmin
import kotlinx.coroutines.launch

class ChatInfoViewModel(
    private val cid: String?,
    userData: UserData?,
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private lateinit var channelClient: ChannelClient
    private val _state = MediatorLiveData<State>()
    private val _channelDeletedState = MutableLiveData(false)
    private val _errorEvents: MutableLiveData<Event<ErrorEvent>> = MutableLiveData()
    val state: LiveData<State> = _state
    val channelDeletedState: LiveData<Boolean> = _channelDeletedState
    val errorEvents: LiveData<Event<ErrorEvent>> = _errorEvents

    init {
        if (cid != null) {
            channelClient = chatClient.channel(cid)
            _state.value = State()
            viewModelScope.launch {
                // Update channel mute status
                chatDomain.user.value?.channelMutes?.let(::updateChannelMuteStatus)

                val channelControllerResult = chatDomain.getChannelController(cid).await()
                if (channelControllerResult.isSuccess) {
                    val channelController = channelControllerResult.data()
                    _state.addSource(channelController.members) { memberList ->
                        // Updates only if the user state is already set
                        _state.value = _state.value!!.copy(canDeleteChannel = memberList.isCurrentUserOwnerOrAdmin())
                        memberList.find { member -> member.user.id == _state.value?.member?.user?.id }?.let { member ->
                            _state.value = _state.value?.copy(member = member)
                        }
                    }
                }
                // Currently, we don't receive any event when channel member is banned/shadow banned, so
                // we need to get member data from the server
                val result =
                    channelClient.queryMembers(
                        offset = 0,
                        limit = 1,
                        filter = Filters.ne("id", chatDomain.currentUser.id)
                    )
                        .await()
                if (result.isSuccess) {
                    val member = result.data().firstOrNull()
                    // Update member, member block status, and channel notifications
                    _state.value = _state.value!!.copy(
                        member = member,
                        isMemberBlocked = member?.shadowBanned ?: false,
                        loading = false,
                    )
                } else {
                    // TODO: Handle error
                    _state.value = _state.value!!.copy(loading = false)
                }
            }
        } else {
            _state.value =
                State(
                    member = Member(user = userData!!.toUser()),
                    canDeleteChannel = false,
                    channelExists = false,
                    loading = false,
                )
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.OptionMuteDistinctChannelClicked -> switchChannelMute(action.isEnabled)
            is Action.OptionBlockUserClicked -> switchUserBlock(action.isEnabled)
            is Action.ChannelMutesUpdated -> updateChannelMuteStatus(action.channelMutes)
            is Action.ChannelDeleted -> deleteChannel()
        }
    }

    private fun updateChannelMuteStatus(channelMutes: List<ChannelMute>) {
        _state.value = _state.value!!.copy(channelMuted = channelMutes.any { it.channel.cid == cid })
    }

    private fun switchChannelMute(isEnabled: Boolean) {
        viewModelScope.launch {
            val result = if (isEnabled) {
                channelClient.mute().await()
            } else {
                channelClient.unmute().await()
            }
            if (result.isError) {
                _errorEvents.postValue(Event(ErrorEvent.MuteChannelError))
            }
        }
    }

    private fun switchUserBlock(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentState = _state.value!!
            if (currentState.member == null) {
                return@launch
            }
            val result = if (isEnabled) {
                channelClient.shadowBanUser(
                    targetId = currentState.member.getUserId(),
                    reason = null,
                    timeout = null
                ).await()
            } else {
                channelClient.removeShadowBan(currentState.member.getUserId()).await()
            }
            if (result.isError) {
                _errorEvents.postValue(Event(ErrorEvent.BlockUserError))
            }
        }
    }

    private fun deleteChannel() {
        val cid = requireNotNull(cid)
        viewModelScope.launch {
            val result = chatDomain.deleteChannel(cid).await()
            if (result.isSuccess) {
                _channelDeletedState.value = true
            } else {
                _errorEvents.postValue(Event(ErrorEvent.DeleteChannelError))
            }
        }
    }

    data class State(
        val member: Member? = null,
        val channelMuted: Boolean = false,
        val isMemberBlocked: Boolean = false,
        val canDeleteChannel: Boolean = false,
        val channelExists: Boolean = true,
        val loading: Boolean = true,
    )

    sealed class Action {
        data class OptionMuteDistinctChannelClicked(val isEnabled: Boolean) : Action()
        data class OptionBlockUserClicked(val isEnabled: Boolean) : Action()
        data class ChannelMutesUpdated(val channelMutes: List<ChannelMute>) : Action()
        object ChannelDeleted : Action()
    }

    sealed class ErrorEvent {
        object MuteChannelError : ErrorEvent()
        object BlockUserError : ErrorEvent()
        object DeleteChannelError : ErrorEvent()
    }
}

class ChatInfoViewModelFactory(private val cid: String?, private val userData: UserData?) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(cid != null || userData != null) {
            "Either cid or userData should not be null"
        }
        require(modelClass == ChatInfoViewModel::class.java) {
            "ChatInfoViewModelFactory can only create instances of ChatInfoViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChatInfoViewModel(cid, userData) as T
    }
}
