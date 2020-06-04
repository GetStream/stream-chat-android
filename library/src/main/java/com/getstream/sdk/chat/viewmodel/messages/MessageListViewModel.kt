package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations.map
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

private const val MESSAGES_LIMIT = 30

class MessageListViewModel(private val cid: String,
                           private val domain: ChatDomain = ChatDomain.instance(),
                           private val client: ChatClient = ChatClient.instance()) : ViewModel() {
    private val threadMessages: MutableLiveData<List<Message>> = MutableLiveData()
    private val channelController: ChannelController

    val state: LiveData<State>
    val channel: Channel
    val currentUser: User

    init {
        val result = domain.useCases.watchChannel.invoke(cid, MESSAGES_LIMIT).execute()
        channelController = result.data()
        channel = channelController.toChannel()
        currentUser = domain.currentUser

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
                domain.useCases.loadOlderMessages(cid, MESSAGES_LIMIT)
            }
            is Event.ThreadEndRegionReached -> {
                domain.useCases.threadLoadMore(cid, event.parentMessage.id, MESSAGES_LIMIT).execute()
            }
            is Event.LastMessageRead -> {
                domain.useCases.markRead.invoke(cid).execute()
            }
            is Event.ThreadModeEntered -> {
                TODO("Not implemented")
            }
            is Event.DeleteMessage -> {
                domain.useCases.deleteMessage(event.message).execute()
            }
            is Event.FlagMessage -> {
                client.flag(event.message.user.id).enqueue()
            }
        }
    }

    sealed class State {
        object Loading : State()
        data class Result(val messageListItem: MessageListItemWrapper) : State()
    }

    sealed class Event {
        object EndRegionReached : Event()
        data class ThreadEndRegionReached(val parentMessage: Message) : Event()
        object LastMessageRead : Event()
        data class ThreadModeEntered(val parentMessage: Message) : Event()
        data class DeleteMessage(val message: Message) : Event()
        data class FlagMessage(val message: Message) : Event()
    }
}