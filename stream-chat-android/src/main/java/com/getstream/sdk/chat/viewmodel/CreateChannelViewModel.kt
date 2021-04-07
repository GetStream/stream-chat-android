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
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.launch

private val CHANNEL_NAME_REGEX = Regex("^!?[\\w-]*\$")

public class CreateChannelViewModel @JvmOverloads constructor(
    private val domain: ChatDomain = ChatDomain.instance(),
    private val client: ChatClient = ChatClient.instance(),
) : ViewModel() {
    private val stateMerger = MediatorLiveData<State>()
    public val state: LiveData<State> = stateMerger

    public fun onEvent(event: Event) {
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
        viewModelScope.launch(DispatcherProvider.IO) {
            val result = domain.createChannelCall(channel).execute()
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

    public sealed class State {
        public object Loading : State()
        public object ChannelCreated : State()
        public object BackendError : State()
        public object ValidationError : State()
    }

    public sealed class Event {
        public data class ChannelNameSubmitted(val channelName: String) : Event()
    }
}
