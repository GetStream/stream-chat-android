package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.extensions.watchChannelAsState
import io.getstream.chat.ui.sample.util.extensions.isAnonymousChannel

class ChatViewModel(
    private val cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    @OptIn(InternalStreamChatApi::class)
    private val channelState: ChannelState = chatClient.watchChannelAsState(cid, DEFAULT_MESSAGE_LIMIT, viewModelScope)

    private val _navigationEvent: MutableLiveData<Event<NavigationEvent>> = MutableLiveData()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    @OptIn(InternalStreamChatApi::class)
    val members: LiveData<List<Member>> = channelState.members.asLiveData()

    @OptIn(InternalStreamChatApi::class)
    fun onAction(action: Action) {
        when (action) {
            is Action.HeaderClicked -> {
                val channelData = requireNotNull(channelState.channelData.value)
                _navigationEvent.value = Event(
                    if (action.members.size > 2 || !channelData.isAnonymousChannel()) {
                        NavigationEvent.NavigateToGroupChatInfo(cid)
                    } else {
                        NavigationEvent.NavigateToChatInfo(cid)
                    }
                )
            }
        }
    }

    sealed class Action {
        class HeaderClicked(val members: List<Member>) : Action()
    }

    sealed class NavigationEvent {
        abstract val cid: String

        data class NavigateToChatInfo(override val cid: String) : NavigationEvent()
        data class NavigateToGroupChatInfo(override val cid: String) : NavigationEvent()
    }

    private companion object {

        /**
         * The default limit for messages count in requests.
         */
        private const val DEFAULT_MESSAGE_LIMIT: Int = 30
    }
}
