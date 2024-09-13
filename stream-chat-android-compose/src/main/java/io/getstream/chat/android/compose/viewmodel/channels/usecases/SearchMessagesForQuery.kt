package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.compose.viewmodel.channels.SearchMessageState
import io.getstream.chat.android.core.utils.Debouncer
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.uiutils.extension.defaultChannelListFilter
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull

internal class SearchMessagesForQuery(
    private val chatClient: ChatClient,
    private val logger: TaggedLogger,
    private val channelLimit: Int,
    private val iChannelViewState: IChannelViewState,
) : IChannelViewState by iChannelViewState {
    internal suspend operator fun invoke(
        coroutineScope: CoroutineScope,
        query: String,
        searchDebouncer: Debouncer,
    ) {
        coroutineScope.coroutineContext.cancelChildren()
        handleSearchQuery(query, searchDebouncer)
        observeSearchMessages(query)
    }

    private fun handleSearchQuery(query: String, searchDebouncer: Debouncer) {
        logger.d { "[handleSearchQuery] query: '$query'" }
        searchDebouncer.submitSuspendable {
            searchMessagesForQuery(query)
        }
    }

    private suspend fun searchMessagesForQuery(query: String) {
        logger.d { "[searchMessagesForQuery] query: '$query'" }
        val channelFilter =
            filterFlow.value ?: Filters.defaultChannelListFilter(chatClient.clientState.user.value) ?: run {
                logger.v { "[searchMessagesForQuery] rejected (no channel filter)" }
                return
            }
        val newState = SearchMessageState(query = query, isLoading = true)
        searchMessageState.value = newState
        searchMessageState.value =
            SearchMessages(logger, chatClient)(src = "new", newState, channelFilter, channelLimit).also {
                logger.v { "[searchMessagesForQuery] completed('$query'): ${it.messages.size}" }
            }
    }

    private suspend fun observeSearchMessages(query: String) = runCatching {
        logger.d { "[observeSearchMessages] query: '$query'" }
        searchMessageState.filterNotNull().collectLatest {
            logger.v { "[observeSearchMessages] state: ${it.stringify()}" }
            channelsState = channelsState.copy(
                searchQuery = searchQuery.value,
                isLoading = it.isLoading,
                isLoadingMore = it.isLoadingMore,
                endOfChannels = !it.canLoadMore,
                channelItems = it.messages.map(ItemState::SearchResultItemState),
            )
        }
    }.onFailure {
        when (it is CancellationException) {
            true -> logger.v { "[observeSearchMessages] cancelled('$query')" }
            else -> logger.e { "[observeSearchMessages] failed: $it" }
        }
    }

    suspend fun loadMoreQueryMessages() {
        logger.d { "[loadMoreQueryMessages] no args" }
        val channelFilter =
            filterFlow.value ?: Filters.defaultChannelListFilter(chatClient.clientState.user.value) ?: run {
                logger.v { "[loadMoreQueryMessages] rejected (no channel filter)" }
                return
            }
        val currentState = searchMessageState.value ?: run {
            logger.v { "[loadMoreQueryMessages] rejected (no current state)" }
            return
        }
        if (currentState.isLoading) {
            logger.v { "[loadMoreQueryMessages] rejected (already loading)" }
            return
        }
        if (currentState.isLoadingMore) {
            logger.v { "[loadMoreQueryMessages] rejected (already loading more)" }
            return
        }
        if (!currentState.canLoadMore) {
            logger.v { "[loadMoreQueryMessages] rejected (end of messages)" }
            return
        }
        val query = currentState.query
        logger.v { "[loadMoreQueryMessages] query: 'query'" }
        val newState = currentState.copy(isLoadingMore = true)
        searchMessageState.value = newState
        searchMessageState.value = SearchMessages(logger, chatClient).invoke(
            src = "more",
            currentState = newState,
            channelFilter = channelFilter,
            channelLimit = channelLimit,
        ).also {
            logger.v { "[loadMoreQueryMessages] completed('$query'): ${it.messages.size}" }
        }
    }
}
