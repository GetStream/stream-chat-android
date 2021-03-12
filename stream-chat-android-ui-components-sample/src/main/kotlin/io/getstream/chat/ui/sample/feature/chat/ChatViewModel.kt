package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.utils.Event
import io.getstream.chat.ui.sample.util.extensions.isAnonymousChannel

class ChatViewModel(private val cid: String, private val chatDomain: ChatDomain = ChatDomain.instance()) : ViewModel() {

    private val _navigationEvent: MutableLiveData<Event<NavigationEvent>> = MutableLiveData()
    private var channelController: ChannelController? = null
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent

    init {
        chatDomain.useCases.getChannelController(cid).enqueue { result ->
            if (result.isSuccess) {
                channelController = result.data()
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.HeaderClicked -> {
                val controller = requireNotNull(channelController)
                controller.members.value?.let { members ->
                    _navigationEvent.value = Event(
                        if (members.size > 2 ||
                            controller.channelData.value?.isAnonymousChannel() == false
                        ) {
                            NavigationEvent.NavigateToGroupChatInfo(cid)
                        } else {
                            NavigationEvent.NavigateToChatInfo(cid)
                        }
                    )
                }
            }
        }
    }

    sealed class Action {
        object HeaderClicked : Action()
    }

    sealed class NavigationEvent {
        abstract val cid: String

        data class NavigateToChatInfo(override val cid: String) : NavigationEvent()
        data class NavigateToGroupChatInfo(override val cid: String) : NavigationEvent()
    }
}
