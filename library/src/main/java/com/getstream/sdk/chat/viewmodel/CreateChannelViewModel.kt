package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain

class CreateChannelViewModel(
        private val domain: ChatDomain = ChatDomain.instance(),
        private val client: ChatClient = ChatClient.instance()
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    val state: LiveData<State> = stateMerger

    fun onEvent(event: Event) {
        if (event is Event.ChannelNameSubmitted) {
            val channelNameCandidate = event.channelName
            val isValidName = validateChannelName(channelNameCandidate)
            if (isValidName) {
                stateMerger.postValue(State.Loading)
                queryChannel(channelNameCandidate)
            } else {
                stateMerger.postValue(State.ValidationError)
            }
        }
    }

    private fun queryChannel(channelName: String) {
        val channelId: String = channelName.replace(" ".toRegex(), "-").toLowerCase()
        val members = client.getCurrentUser()?.run {
            listOf(Member(this))
        } ?: listOf()
        val channel = Channel().apply {
            this.cid = "messaging:$channelId"
            this.id = channelId
            this.type = "messaging"
            this.name = channelName
            this.members = members
        }
        domain.useCases.createChannel.invoke(channel).enqueue { result ->
            when {
                result.isSuccess -> {
                    stateMerger.postValue(State.ChannelCreated)
                }
                result.isError -> {
                    stateMerger.postValue(State.BackendError)
                }
            }
        }
    }

    private fun validateChannelName(channelNameCandidate: String): Boolean {
        return channelNameCandidate.isNotEmpty() && channelNameCandidate.matches(Regex("^!?[\\w-]*\$"))
    }

    sealed class State {
        object Loading : State()
        object ChannelCreated : State()
        object BackendError : State()
        object ValidationError : State()
    }

    sealed class Event {
        data class ChannelNameSubmitted(val channelName: String) : Event()
    }
}