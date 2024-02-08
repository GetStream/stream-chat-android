package io.getstream.chat.docs.kotlin.cookbook.ui

import androidx.lifecycle.ViewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.result.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class CustomChannelListViewModel(val chatClient: ChatClient = ChatClient.instance()) : ViewModel() {
    private val _uiState = MutableStateFlow(ChannelListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Get last conversations I participated in, sorted by last updated
        val request = QueryChannelsRequest(
            filter = Filters.and(
                Filters.`in`("members", listOf("filip")),
            ),
            offset = 0,
            limit = 30,
            querySort = QuerySortByField.descByName("last_updated")
        ).apply {
            watch = true
            state = true
        }

        chatClient.queryChannels(request).enqueue { result ->
            when (result) {
                is Result.Success -> {
                    val channels: List<Channel> = result.value
                    _uiState.update { it.copy(channels = channels, error = null) }
                }
                else -> {
                    _uiState.update { it.copy(error = "Error loading channels") }
                }
            }
        }
    }
}

data class ChannelListUiState(
    val channels: List<Channel> = emptyList(),
    val error: String? = null,
)