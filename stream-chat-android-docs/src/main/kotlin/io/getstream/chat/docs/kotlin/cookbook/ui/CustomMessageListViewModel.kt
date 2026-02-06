package io.getstream.chat.docs.kotlin.cookbook.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.client.api.state.loadOlderMessages
import io.getstream.chat.android.client.api.state.watchChannelAsState
import io.getstream.result.call.enqueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomMessageListViewModel(val chatClient: ChatClient = ChatClient.instance()) : ViewModel() {
    private val _uiState = MutableStateFlow(MessageListUiState())
    val uiState = _uiState.asStateFlow()

    fun getMessages(cid: String) {
        val channelStateFlow: StateFlow<ChannelState?> = chatClient.watchChannelAsState(
            cid = cid,
            messageLimit = 30,
            coroutineScope = viewModelScope
        )

        viewModelScope.launch {
            channelStateFlow.collect { channelState ->
                if (channelState != null) {
                    channelState.messages.collect { messages ->
                        _uiState.update {
                            it.copy(messages = messages.reversed(), error = null)
                        }
                        Log.d("[Messages]", "Count: ${uiState.value.messages.size}")
                    }
                } else {
                    _uiState.update { it.copy(error = "Cannot load messages") }
                }
            }
        }
    }

    fun loadMoreMessages(cid: String) {
        chatClient.loadOlderMessages(cid = cid, messageLimit = 30).enqueue(
            onError = { streamError ->
                Log.e("[Messages]", "Cannot load more messages. Error: ${streamError.message}")
            }
        )
    }

    fun getMessages2(cid: String) {
        val query = QueryChannelRequest()
            .withMessages(30)
            .withWatch()
        val channelClient = chatClient.channel(cid)

        channelClient.query(query).enqueue {
            it.onSuccess {
                it.messages
            }
        }
    }
}

data class MessageListUiState(
    val messages: List<Message> = emptyList(),
    val error: String? = null,
)
