package io.getstream.chat.android.compose.viewmodel.channels

import io.getstream.chat.android.models.Message

internal data class SearchMessageState(
    val query: String = "",
    val canLoadMore: Boolean = true,
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
) {

    fun stringify(): String {
        return "SearchMessageState(" +
            "query='$query', " +
            "messages.size=${messages.size}, " +
            "isLoading=$isLoading, " +
            "isLoadingMore=$isLoadingMore, " +
            "canLoadMore=$canLoadMore)"
    }
}