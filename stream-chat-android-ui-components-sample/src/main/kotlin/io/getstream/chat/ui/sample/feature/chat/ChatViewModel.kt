package io.getstream.chat.ui.sample.feature.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.ui.sample.util.extensions.isAnonymousChannel

class ChatViewModel(private val cid: String, private val chatDomain: ChatDomain = ChatDomain.instance()) : ViewModel() {

    private val _navigationEvent: MutableLiveData<Event<NavigationEvent>> = MutableLiveData()
    private var channelController: ChannelController? = null
    val navigationEvent: LiveData<Event<NavigationEvent>> = _navigationEvent
    private val _members: MediatorLiveData<List<Member>> = MediatorLiveData()
    val members: LiveData<List<Member>> = _members

    init {
        chatDomain.getChannelController(cid).enqueue { result ->
            if (result.isSuccess) {
                channelController = result.data().also {
                    _members.addSource(it.members) { members -> _members.value = members }
                }
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            is Action.HeaderClicked -> {
                val controller = requireNotNull(channelController)
                _navigationEvent.value = Event(
                    if (action.members.size > 2 ||
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

    sealed class Action {
        class HeaderClicked(val members: List<Member>) : Action()
    }

    sealed class NavigationEvent {
        abstract val cid: String

        data class NavigateToChatInfo(override val cid: String) : NavigationEvent()
        data class NavigateToGroupChatInfo(override val cid: String) : NavigationEvent()
    }
}
