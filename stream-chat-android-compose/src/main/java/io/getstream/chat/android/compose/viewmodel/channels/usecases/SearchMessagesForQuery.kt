package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.compose.viewmodel.channels.delegates.IHelpSearchWithDebounce
import io.getstream.chat.android.compose.viewmodel.channels.delegates.StreamMessagesLoader
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch

internal class SearchMessagesForQuery(
    chatClient: ChatClient,
    private val logger: TaggedLogger,
    channelLimit: Int,
    private val iChannelViewState: IChannelViewState,
    private val iHelpSearchWithDebounce: IHelpSearchWithDebounce,
) : IChannelViewState by iChannelViewState, IHelpSearchWithDebounce by iHelpSearchWithDebounce {

    private val iLoadMessages =
        StreamMessagesLoader(
            iChannelViewState = iChannelViewState,
            searchDebouncer = searchDebouncer,
            logger = logger,
            chatClient = chatClient,
            channelLimit = channelLimit
        )

    internal suspend operator fun invoke(
        query: String,
    ) {
        chListScope.coroutineContext.cancelChildren()
        iLoadMessages.load(query)
    }

    fun loadMoreQueryMessages() {
        searchScope.launch(CoroutineExceptionHandler { _, throwable ->
            logger.e(throwable) {
                "failed to loadMoreQueryMessages"
            }
        }) {
            iLoadMessages.loadMore()
        }
    }
}
