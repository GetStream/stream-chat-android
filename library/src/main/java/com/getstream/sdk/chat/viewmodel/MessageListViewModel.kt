package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.getstream.sdk.chat.utils.MessageListItemLiveData
import com.getstream.sdk.chat.utils.MessageListItemWrapper
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain

private const val MESSAGES_LIMIT = 30
class MessageListViewModel(private val cid: String,
                           private val domain: ChatDomain = ChatDomain.instance()) : ViewModel() {
    private val threadMessages: MutableLiveData<List<Message>> = MutableLiveData()
    private val loading: MutableLiveData<State> = MutableLiveData()
    private val stateMerger = MediatorLiveData<State>()
    val state: LiveData<State> = stateMerger

    init {
        val channelController = domain.useCases.watchChannel(cid, MESSAGES_LIMIT).execute().data()
        val currentUser = domain.currentUser
        val listItems = MessageListItemLiveData(
                currentUser,
                channelController.messages,
                threadMessages,
                channelController.typing,
                channelController.reads
        )
        loading.postValue(State.Loading)
        stateMerger.addSource(listItems) {
            State.Result(it)
        }
        stateMerger.addSource(loading) { stateMerger.postValue(it) }
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