package io.getstream.chat.ui.sample.feature.chat.preview

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.offline.utils.Event
import io.getstream.chat.ui.sample.common.CHANNEL_ARG_DRAFT
import kotlinx.coroutines.launch

class ChatPreviewViewModel(
    private val memberId: String,
    private val chatDomain: ChatDomain = ChatDomain.instance(),
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private var cid: String? = null
    private val _state: MutableLiveData<State> = MutableLiveData()
    private val _events: MutableLiveData<Event<UiEvent>> = MutableLiveData()
    val state: LiveData<State> = _state
    val events = _events

    init {
        _state.value = State(cid = null)
        viewModelScope.launch {
            val result = chatClient.createChannel(
                channelType = "messaging",
                members = listOf(chatDomain.currentUser.id, memberId),
                extraData = mapOf(CHANNEL_ARG_DRAFT to true)
            ).await()
            if (result.isSuccess) {
                cid = result.data().cid
                _state.value = State(cid!!)
            }
        }
    }

    fun onAction(action: Action) {
        when (action) {
            Action.MessageSent -> updateChannel()
        }
    }

    private fun updateChannel() {
        val cid = requireNotNull(cid)
        viewModelScope.launch {
            val result =
                chatClient.channel(cid).update(message = null, extraData = mapOf(CHANNEL_ARG_DRAFT to false)).await()
            if (result.isSuccess) {
                _events.value = Event(UiEvent.NavigateToChat(cid))
            }
        }
    }

    data class State(val cid: String?)

    sealed class Action {
        object MessageSent : Action()
    }

    sealed class UiEvent {
        data class NavigateToChat(val cid: String) : UiEvent()
    }
}

class ChatPreviewViewModelFactory(private val memberId: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == ChatPreviewViewModel::class.java) {
            "ChatPreviewViewModelFactory can only create instances of ChatPreviewViewModel"
        }

        @Suppress("UNCHECKED_CAST")
        return ChatPreviewViewModel(memberId) as T
    }
}
