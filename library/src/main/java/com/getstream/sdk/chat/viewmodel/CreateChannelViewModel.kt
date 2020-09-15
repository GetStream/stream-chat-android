package com.getstream.sdk.chat.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

private val CHANNEL_NAME_REGEX = Regex("^!?[\\w-]*\$")

class CreateChannelViewModel @JvmOverloads constructor(
    private val domain: ChatDomain = ChatDomain.instance(),
    private val client: ChatClient = ChatClient.instance(),
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    val state: LiveData<State> = stateMerger

    fun onEvent(event: Event) {
        if (event is Event.ChannelNameSubmitted) {
            val channelNameCandidate = event.channelName.replace(" ".toRegex(), "-").toLowerCase()
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
        val author = client.getCurrentUser() ?: User()
        val members = listOf(Member(author))
        val channel = Channel().apply {
            this.cid = "messaging:$channelName"
            this.id = channelName
            this.type = "messaging"
            this.name = channelName
            this.members = members
            this.createdBy = author
        }
        viewModelScope.launch(ioDispatcher) {
            val result = domain.useCases.createChannel.invoke(channel).execute()
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
        return channelNameCandidate.isNotEmpty() && channelNameCandidate.matches(CHANNEL_NAME_REGEX)
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
