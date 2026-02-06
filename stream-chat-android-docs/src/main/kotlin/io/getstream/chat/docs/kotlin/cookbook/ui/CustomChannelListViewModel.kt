package io.getstream.chat.docs.kotlin.cookbook.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.client.api.state.queryChannelsAsState
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.result.call.enqueue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomChannelListViewModel(val chatClient: ChatClient = ChatClient.instance()) : ViewModel() {
    private val _uiState = MutableStateFlow(ChannelListUiState())
    val uiState = _uiState.asStateFlow()

    private var queryChannelsStateFlow: StateFlow<QueryChannelsState?> = MutableStateFlow(null)

    init {
        // Get last conversations I participated in, sorted by last updated
        val request = QueryChannelsRequest(
            filter = Filters.and(
                Filters.`in`("members", listOf("filip")),
            ),
            offset = 0,
            limit = 30,
            querySort = QuerySortByField.descByName("last_updated")
        )

        queryChannelsStateFlow = chatClient.queryChannelsAsState(request, coroutineScope = viewModelScope)

        viewModelScope.launch {
            queryChannelsStateFlow.collect{ queryChannelsState ->
                if (queryChannelsState != null) {
                    queryChannelsState.channels.collect { channels ->
                        channels?.let {
                            _uiState.update { it.copy(channels = channels, error = null) }
                            Log.d("[Channels]", "Count: ${uiState.value.channels.size}")
                        }
                    }
                } else {
                    _uiState.update { it.copy(error = "Cannot load channels") }
                }
            }
        }

        // region Old
        // Get last conversations I participated in, sorted by last updated
        // val request = QueryChannelsRequest(
        //     filter = Filters.and(
        //         Filters.`in`("members", listOf("filip")),
        //     ),
        //     offset = 0,
        //     limit = 30,
        //     querySort = QuerySortByField.descByName("last_updated")
        // ).apply {
        //     watch = true
        //     state = true
        // }
        //
        // chatClient.queryChannels(request).enqueue { result ->
        //     when (result) {
        //         is Result.Success -> {
        //             val channels: List<Channel> = result.value
        //             _uiState.update { it.copy(channels = channels, error = null) }
        //         }
        //         else -> {
        //             _uiState.update { it.copy(error = "Error loading channels") }
        //         }
        //     }
        // }
        // endregion
    }

    fun loadMoreChannels() {
        val queryChannelsState = queryChannelsStateFlow.value ?: return

        queryChannelsState.nextPageRequest.value?.let {
            chatClient.queryChannels(it).enqueue(
                onError = { streamError ->
                    Log.e("[Channels]", "Cannot load more channels. Error: ${streamError.message}")
                },
            )
        }
    }
}

data class ChannelListUiState(
    val channels: List<Channel> = emptyList(),
    val error: String? = null,
)