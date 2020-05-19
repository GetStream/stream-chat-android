package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel

class CreateChannelViewModel : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    val state: LiveData<State> = stateMerger

    sealed class State {
        object ChannelCreated : State()
        object Error : State()
    }

    sealed class Event {
        object ChannelNameSubmitted : Event()
    }
}