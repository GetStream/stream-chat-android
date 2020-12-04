package io.getstream.chat.ui.sample.feature.chat.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.ChatDomain

class ChatInfoViewModel(private val cid: String, private val chatDomain: ChatDomain = ChatDomain.instance()) :
    ViewModel() {

    private val _state = MediatorLiveData<State>()
    val state: LiveData<State> = _state

    init {
        val controller = chatDomain.useCases.getChannelController(cid).execute().data()
        _state.addSource(map(controller.channelData) { mapChannelToState(controller.toChannel()) }) {
            _state.value = it
        }
    }

    private fun mapChannelToState(channel: Channel): State {
        return State(
            member = channel.members.first {
                it.getUserId() != chatDomain.currentUser.id
            },
            notificationsEnabled = false,
            isMemberMuted = false,
            isMemberBlocked = false,
        )
    }

    data class State(
        val member: Member,
        val notificationsEnabled: Boolean,
        val isMemberMuted: Boolean,
        val isMemberBlocked: Boolean
    )
}
