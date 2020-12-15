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
            // Currently, we don't receive any event when channel member is banned/shadow banned, so
            // we need to get member data from the server
            val result =
                channelClient.queryMembers(offset = 0, limit = 1, filter = Filters.ne("id", chatDomain.currentUser.id))
                    .await()
            if (result.isSuccess) {
                val member = result.data().first()
                // Update member and member block status
                _state.value =
                    _state.value!!.copy(chatMember = ChatMember(member, false), isMemberBlocked = member.shadowBanned)
                // Update channel notifications
                updateChannelNotificationsStatus(chatDomain.currentUser.channelMutes)

                // Muted channel members
                _state.addSource(chatDomain.muted) { mutes ->
                    val currentState = state.value!!
                    _state.value =
                        currentState.copy(isMemberMuted = mutes.any { it.target.id == currentState.chatMember.member.getUserId() })
                }
            } else {
                // TODO: Handle error
            }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.OptionNotificationClicked -> switchNotifications(event.isEnabled)
            is Event.OptionMuteUserClicked -> switchUserMute(event.isEnabled)
            is Event.OptionBlockUserClicked -> switchUserBlock(event.isEnabled)
            is Event.ChannelMutesUpdated -> updateChannelNotificationsStatus(event.channelMutes)
            is Event.DeleteChannel -> deleteChannel()
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
                channelClient.muteUser(currentState.chatMember.member.getUserId()).await()
            } else {
                channelClient.unmuteUser(currentState.chatMember.member.getUserId()).await()
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
                    targetId = currentState.chatMember.member.getUserId(),
                    reason = null,
                    timeout = null
                ).await()
            } else {
                channelClient.removeShadowBan(currentState.chatMember.member.getUserId()).await()
            }
            if (result.isError) {
                // TODO: Show error message
                _state.value = _state.value!!.copy(isMemberBlocked = !isEnabled)
            }
        }
    }

    private fun deleteChannel() {
        viewModelScope.launch {
            val result = chatDomain.useCases.hideChannel.invoke(cid, keepHistory = true).await()
            if (result.isSuccess) {
                _channelDeletedState.value = true
            }
            // Handle error
        }
    }

    data class State(
        val chatMember: ChatMember = ChatMember(member = Member(User())),
        val notificationsEnabled: Boolean = false,
        val isMemberMuted: Boolean = false,
        val isMemberBlocked: Boolean = false
    )

    sealed class Event {
        data class OptionNotificationClicked(val isEnabled: Boolean) : Event()
        data class OptionMuteUserClicked(val isEnabled: Boolean) : Event()
        data class OptionBlockUserClicked(val isEnabled: Boolean) : Event()
        data class ChannelMutesUpdated(val channelMutes: List<ChannelMute>) : Event()
        object DeleteChannel : Event()
    }
}
