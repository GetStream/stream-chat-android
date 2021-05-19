package io.getstream.chat.ui.sample.feature.channel.add.group.select_name

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class AddGroupChannelSelectNameViewModel : ViewModel() {

    private val currentUserId: String = ChatDomain.instance().user.value!!.id
    private val _state: MutableLiveData<State> = MutableLiveData()
    val state: LiveData<State> = _state

    fun onEvent(event: Event) {
        when (event) {
            is Event.CreateChannel -> createChannel(event.name, event.members)
        }
    }

    private fun createChannel(name: String, members: List<User>) {
        _state.value = State.Loading
        viewModelScope.launch(Dispatchers.Main) {
            val result = ChatClient.instance()
                .createChannel(
                    channelType = CHANNEL_TYPE_MESSAGING,
                    channelId = UUID.randomUUID().toString(),
                    members = members.map { it.id } + currentUserId,
                    extraData = mapOf(EXTRA_DATA_CHANNEL_NAME to name)
                ).await()
            if (result.isSuccess) {
                _state.value = State.NavigateToChannel(result.data().cid)
            }
        }
    }

    companion object {
        private const val CHANNEL_TYPE_MESSAGING = "messaging"
        private const val EXTRA_DATA_CHANNEL_NAME = "name"
    }

    sealed class State {
        object Loading : State()
        data class NavigateToChannel(val cid: String) : State()
    }

    sealed class Event {
        data class CreateChannel(val name: String, val members: List<User>) : Event()
    }
}
