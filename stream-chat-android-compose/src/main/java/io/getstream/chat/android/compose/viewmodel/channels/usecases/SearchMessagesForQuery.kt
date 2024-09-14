package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.compose.viewmodel.channels.delegates.StreamMessagesLoader
import io.getstream.chat.android.core.utils.Debouncer
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren

internal class SearchMessagesForQuery(
    chatClient: ChatClient,
    logger: TaggedLogger,
    channelLimit: Int,
    private val iChannelViewState: IChannelViewState,
    searchDebouncer: Debouncer,
) : IChannelViewState by iChannelViewState {

    private val iLoadMessages =
        StreamMessagesLoader(iChannelViewState, searchDebouncer, logger, chatClient, channelLimit)

    internal suspend operator fun invoke(
        coroutineScope: CoroutineScope,
        query: String,
    ) {
        coroutineScope.coroutineContext.cancelChildren()
        iLoadMessages.load(query)
    }

    suspend fun loadMoreQueryMessages() {
        iLoadMessages.loadMore()
    }
}
