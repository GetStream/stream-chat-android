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
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.utils.extensions.isCurrentUserOwnerOrAdmin
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
    val state: LiveData<State> = _state
    val channelDeletedState: LiveData<Boolean> = _channelDeletedState

    init {
        if (cid != null) {
            channelClient = chatClient.channel(cid)
            _state.value = State()
            viewModelScope.launch {
                val channelControllerResult = chatDomain.useCases.getChannelController(cid).await()
                if (channelControllerResult.isSuccess) {
                    val canDeleteChannel = channelControllerResult.data().members.value.isCurrentUserOwnerOrAdmin()
                    _state.value = _state.value!!.copy(canDeleteChannel = canDeleteChannel)
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
                        notificationsEnabled = chatDomain.currentUser.channelMutes.any { it.channel.cid == cid },
                        loading = false,
                    )

                    // Muted channel members
                    _state.addSource(chatDomain.muted) { mutes -> updateMutes(mutes) }
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
            is Action.OptionNotificationClicked -> switchNotifications(action.isEnabled)
            is Action.OptionMuteUserClicked -> switchUserMute(action.isEnabled)
            is Action.OptionBlockUserClicked -> switchUserBlock(action.isEnabled)
            is Action.ChannelMutesUpdated -> updateChannelNotificationsStatus(action.channelMutes)
            is Action.ChannelDeleted -> deleteChannel()
        }
    }

    private fun updateChannelNotificationsStatus(channelMutes: List<ChannelMute>) {
        _state.value = _state.value!!.copy(notificationsEnabled = channelMutes.any { it.channel.cid == cid })
    }

    private fun switchNotifications(isEnabled: Boolean) {
        viewModelScope.launch {
            val result = if (isEnabled) {
                channelClient.mute().await()
            } else {
                channelClient.unmute().await()
            }
            if (result.isError) {
                // Handle error in a better way
                _state.value = _state.value!!.copy(notificationsEnabled = !isEnabled)
            }
        }
    }

    private fun switchUserMute(isEnabled: Boolean) {
        viewModelScope.launch {
            val currentState = _state.value!!
            if (currentState.member == null) {
                return@launch
            }
            val result = if (isEnabled) {
                channelClient.muteUser(currentState.member.getUserId()).await()
            } else {
                channelClient.unmuteUser(currentState.member.getUserId()).await()
            }
            if (result.isError) {
                // Handle error in a better way
                _state.value = _state.value!!.copy(isMemberMuted = !isEnabled)
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
                // TODO: Show error message
                _state.value = _state.value!!.copy(isMemberBlocked = !isEnabled)
            }
        }
    }

    private fun deleteChannel() {
        val cid = requireNotNull(cid)
        viewModelScope.launch {
            val result = chatDomain.useCases.deleteChannel(cid).await()
            if (result.isSuccess) {
                _channelDeletedState.value = true
            }
            // Handle error
        }
    }

    private fun updateMutes(mutes: List<Mute>) {
        val currentState = state.value!!
        if (currentState.member == null) {
            return
        }
        _state.value =
            currentState.copy(isMemberMuted = mutes.any { mute -> mute.target.id == currentState.member.getUserId() })
    }

    data class State(
        val member: Member? = null,
        val notificationsEnabled: Boolean = false,
        val isMemberMuted: Boolean = false,
        val isMemberBlocked: Boolean = false,
        val canDeleteChannel: Boolean = false,
        val channelExists: Boolean = true,
        val loading: Boolean = true,
    )

    sealed class Action {
        data class OptionNotificationClicked(val isEnabled: Boolean) : Action()
        data class OptionMuteUserClicked(val isEnabled: Boolean) : Action()
        data class OptionBlockUserClicked(val isEnabled: Boolean) : Action()
        data class ChannelMutesUpdated(val channelMutes: List<ChannelMute>) : Action()
        object ChannelDeleted : Action()
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
