package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.android.offline.extensions.watchChannelAsState
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.ui.sample.util.extensions.isAnonymousChannel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest

class ChatViewModel(
    private val cid: String,
    chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    /**
     * Holds information about the current channel and is actively updated.
     */
    private val channelState: StateFlow<ChannelState?> =
        chatClient.watchChannelAsState(cid, DEFAULT_MESSAGE_LIMIT, viewModelScope)

    private val _navigationEvent: MutableLiveData<Event<NavigationEvent>> = MutableLiveData()
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    val members: LiveData<List<Member>> = channelState.filterNotNull().flatMapLatest { it.members }.asLiveData()

    fun onAction(action: Action) {
        when (action) {
            is Action.HeaderClicked -> {
                val channelData = requireNotNull(channelState.value?.channelData?.value)
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
