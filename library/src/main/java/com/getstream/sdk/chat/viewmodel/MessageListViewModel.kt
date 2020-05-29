package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.MessageListItemLiveData
import com.getstream.sdk.chat.utils.MessageListItemWrapper
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

private const val MESSAGES_LIMIT = 30
class MessageListViewModel(private val cid: String,
                           private val domain: ChatDomain = ChatDomain.instance()) : ViewModel() {
    private val threadMessages: MutableLiveData<List<Message>> = MutableLiveData()
    private val channelController: ChannelController
    val state: LiveData<State>
    val channel: Channel

    init {
        val result = domain.useCases.watchChannel.invoke(cid, MESSAGES_LIMIT).execute()
        channelController = result.data()
        channel = channelController.toChannel()
        val currentUser = domain.currentUser
        domain.useCases.loadOlderMessages.invoke(cid, MESSAGES_LIMIT).enqueue()

        MessageListItemLiveData(
                currentUser,
                channelController.messages,
                threadMessages,
                channelController.typing,
                channelController.reads
        ).apply {
            state = map(this) { State.Result(it) }
        }
    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.EndRegionReached -> {
                domain.useCases.loadOlderMessages(cid, MESSAGES_LIMIT)// limit was 50 previously
            }
            is Event.ThreadEndRegionReached -> {
                domain.useCases.threadLoadMore(cid, event.parentMessage.id, MESSAGES_LIMIT).execute()
            }
            is Event.EditMessage -> {
            }
            is Event.ThreadModeEntered -> {
            }
            is Event.DeleteMessage -> {
            }
        }
    }

    sealed class State {
        object Loading : State()
        object LoadingMore : State()
        data class Result(val messageListItem: MessageListItemWrapper) : State()
    }

    sealed class Event {
        object EndRegionReached : Event()
        data class ThreadEndRegionReached(val parentMessage: Message): Event()
        object LastMessageRead : Event()
        data class EditMessage(val message: Message) : Event()
        data class ThreadModeEntered(val parentMessage: Message) : Event()
        data class DeleteMessage(val message: Message) : Event()
    }
}

data class MessageListItemResult(
        private var messageListItems: List<MessageListItem> = emptyList(),
        private val isLoadingMore: Boolean = false,
        private val hasNewMessages: Boolean = false,
        private val isTyping: Boolean = false,
        private val isThread: Boolean = false
)