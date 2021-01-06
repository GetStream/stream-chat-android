package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.utils.extensions.isCurrentUserOwnerOrAdmin
import kotlinx.coroutines.launch

class ChatInfoViewModel(
    private val cid: String,
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val chatClient: ChatClient = ChatClient.instance()
) : ViewModel() {

    private val channelClient: ChannelClient = chatClient.channel(cid)
    private val _state = MediatorLiveData<State>()
    private val _channelDeletedState = MutableLiveData(false)
    val state: LiveData<State> = _state
    val channelDeletedState: LiveData<Boolean> = _channelDeletedState

    init {
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
                channelClient.queryMembers(offset = 0, limit = 1, filter = Filters.ne("id", chatDomain.currentUser.id))
                    .await()
            if (result.isSuccess) {
                val member = result.data().first()
                // Update member and member block status
                _state.value = _state.value!!.copy(member = member, isMemberBlocked = member.shadowBanned)
                // Update channel notifications
                updateChannelNotificationsStatus(chatDomain.currentUser.channelMutes)

                // Muted channel members
                _state.addSource(chatDomain.muted) { mutes ->
                    val currentState = state.value!!
                    _state.value =
                        currentState.copy(isMemberMuted = mutes.any { it.target.id == currentState.member.getUserId() })
                }
            } else {
                // TODO: Handle error
            }
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
        viewModelScope.launch {
            val result = chatDomain.useCases.deleteChannel(cid).await()
            if (result.isSuccess) {
                _channelDeletedState.value = true
            }
            // Handle error
        }
    }

    data class State(
        val member: Member = Member(User()),
        val notificationsEnabled: Boolean = false,
        val isMemberMuted: Boolean = false,
        val isMemberBlocked: Boolean = false,
        val canDeleteChannel: Boolean = false,
    )

    sealed class Action {
        data class OptionNotificationClicked(val isEnabled: Boolean) : Action()
        data class OptionMuteUserClicked(val isEnabled: Boolean) : Action()
        data class OptionBlockUserClicked(val isEnabled: Boolean) : Action()
        data class ChannelMutesUpdated(val channelMutes: List<ChannelMute>) : Action()
        object ChannelDeleted : Action()
    }
}
