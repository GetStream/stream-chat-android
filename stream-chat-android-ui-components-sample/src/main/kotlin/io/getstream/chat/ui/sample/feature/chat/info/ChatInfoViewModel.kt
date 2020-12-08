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
        chatDomain.useCases.getChannelController(cid).enqueue { result ->
            if (result.isSuccess) {
                val controller = result.data()
                // Update channel notifications
                updateChannelNotificationsStatus(chatDomain.currentUser.channelMutes)

                // Update members
                _state.addSource(controller.members) { members ->
                    _state.value =
                        _state.value!!.copy(member = members.first { it.getUserId() != chatDomain.currentUser.id })
                }
                // Muted channel members
                _state.addSource(chatDomain.muted) { mutes ->
                    val member = _state.value!!.member
                    _state.value = _state.value?.copy(isMemberMuted = mutes.any { it.target.id == member.getUserId() })
                }
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
        // Shadow ban is not supported yet
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
        val member: Member = Member(User()),
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
