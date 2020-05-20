package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain

class CreateChannelViewModel : ViewModel() {
    private val chatClient = ChatClient.instance()
    private val stateMerger = MediatorLiveData<State>()
    val state: LiveData<State> = stateMerger

    fun onEvent(event: Event) {
        if (event is Event.ChannelNameSubmitted) {
            val channelNameCandidate = event.channelName
            val isValidName = validateChannelName(channelNameCandidate)
            if (isValidName) {
                createChannel(channelNameCandidate)
            } else {
                stateMerger.postValue(State.Error)
            }
        }
    }

    private fun createChannel(channelName: String) {
        val channelId: String = channelName.replace(" ".toRegex(), "-").toLowerCase()
        val members = chatClient.getCurrentUser()?.run {
            listOf(Member(this))
        } ?: listOf()
        val channel = Channel().apply {
            this.cid = "messaging:$channelId"
            this.id = channelId
            this.type = "messaging"
            this.name = channelName
            this.members = members
        }
        ChatDomain.instance().useCases.createChannel.invoke(channel).execute().run {
            when  {
                isSuccess -> {
                    stateMerger.postValue(State.ChannelCreated)
                }
                isError -> {
                    stateMerger.postValue(State.Error)
                }
            }
        }
    }

    private fun validateChannelName(channelNameCandidate: String): Boolean {
        return channelNameCandidate.isNotEmpty()
    }

    sealed class State {
        object ChannelCreated : State()
        object Error : State()
    }

    sealed class Event {
        data class ChannelNameSubmitted(val channelName: String) : Event()
    }
}