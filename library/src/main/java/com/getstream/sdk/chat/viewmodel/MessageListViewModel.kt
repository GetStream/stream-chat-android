package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain

class MessageListViewModel(private val channelId: String,
                           private val domain: ChatDomain = ChatDomain.instance()) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    val state: LiveData<State> = stateMerger

    init {

    }

    fun onEvent(event: Event) {
        when (event) {
            is Event.LoadMore -> {
                // TODO
            }
            is Event.EditMessage -> {}
            is Event.ReplyToMessage -> {}
            is Event.DeleteMessage -> {}
        }
    }
}

sealed class State {
    object Loading : State()
    data class Result(val messages: List<Message>) : State()
}

sealed class Event {
    object LoadMore : Event()
    data class EditMessage(val message: Message) : Event()
    data class ReplyToMessage(val message: Message) : Event()
    data class DeleteMessage(val message: Message) : Event()
}