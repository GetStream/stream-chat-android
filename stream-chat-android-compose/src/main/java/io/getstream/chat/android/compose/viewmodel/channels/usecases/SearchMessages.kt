package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.viewmodel.channels.SearchMessageState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.log.TaggedLogger

internal class SearchMessages(
    private val logger: TaggedLogger,
    private val chatClient: ChatClient,
) {
    /**
     * Searches for messages based on the current query.
     */
    suspend operator fun invoke(
        src: String,
        currentState: SearchMessageState,
        channelFilter: FilterObject,
        channelLimit: Int,
    ): SearchMessageState {
        val offset = currentState.messages.size
        val limit = channelLimit
        logger.v { "[searchMessages] #$src; query: '${currentState.query}', offset: $offset, limit: $limit" }
        val result = chatClient.searchMessages(
            channelFilter = channelFilter,
            messageFilter = Filters.autocomplete("text", currentState.query),
            offset = offset,
            limit = limit,
        ).await()
        return when (result) {
            is io.getstream.result.Result.Success -> {
                logger.v { "[searchMessages] #$src; completed(messages.size: ${result.value.messages.size})" }
                currentState.copy(
                    messages = currentState.messages + result.value.messages,
                    isLoading = false,
                    isLoadingMore = false,
                    canLoadMore = result.value.messages.size >= limit,
                )
            }

            is io.getstream.result.Result.Failure -> {
                logger.e { "[searchMessages] #$src; failed: ${result.value}" }
                currentState.copy(
                    isLoading = false,
                    isLoadingMore = false,
                    canLoadMore = true,
                )
            }
        }
    }
}