package com.getstream.sdk.chat.viewmodel.messages

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.view.messages.MessageListItemWrapper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController

class MessageListViewModel(private val cid: String,
                           private val domain: ChatDomain = ChatDomain.instance(),
                           private val client: ChatClient = ChatClient.instance()) : ViewModel() {
    private var threadMessages: LiveData<List<Message>> = MutableLiveData()
    private val channelController: ChannelController
    private val loading = MutableLiveData<State>()
    private val messages: MessageListItemLiveData
    private val stateMerger = MediatorLiveData<State>()

    val state: LiveData<State> = stateMerger
    val channel: Channel
    val currentUser: User

    init {
        loading.value = State.Loading

        val result = domain.useCases.watchChannel.invoke(cid, MESSAGES_LIMIT).execute()
        channelController = result.data()
        channel = channelController.toChannel()
        currentUser = domain.currentUser

        messages = MessageListItemLiveData(
                currentUser,
                channelController.messages,
                threadMessages,
                channelController.typing,
                channelController.reads
        )

        stateMerger.apply {
            addSource(loading) { value = it }
            addSource(messages) { value = State.Result(it) }
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
                onThreadModeEntered(event.parentMessage)
            }
            is Event.DeleteMessage -> {
                domain.useCases.deleteMessage(event.message).execute()
            }
            is Event.FlagMessage -> {
                client.flag(event.message.user.id).enqueue()
            }
        }
    }

    private fun onThreadModeEntered(parentMessage: Message) {
        val parentId: String = parentMessage.id
        val threadController = domain.useCases.getThread.invoke(cid, parentId).execute().data()
        messages.setThreadMessages(threadController.messages)
        domain.useCases.threadLoadMore.invoke(cid, parentId, 30).execute()
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

    companion object {
        internal const val MESSAGES_LIMIT = 30
    }
}