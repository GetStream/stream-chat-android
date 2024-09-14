package io.getstream.chat.android.compose.viewmodel.channels.loadchannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.compose.viewmodel.channels.delegates.IHelpSearchWithDebounce
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

internal class SearchChannelsForQuery(
    private val channelLimit: Int,
    private val messageLimit: Int,
    private val memberLimit: Int,
    chatClient: ChatClient,
    chatEventHandlerFactory: ChatEventHandlerFactory,
    private val channelState: IChannelViewState,
    private val logger: TaggedLogger,
    private val iHelpSearchWithDebounce: IHelpSearchWithDebounce,
) : IChannelViewState by channelState,
    IHelpSearchWithDebounce by iHelpSearchWithDebounce {

    private val channelLoader: ILoadChannels = StreamChannelLoader(
        chatClient = chatClient,
        chatEventHandlerFactory = chatEventHandlerFactory,
        chListScope = chListScope,
        channelState = channelState,
        logger = logger
    )

    internal suspend operator fun invoke(
        config: QueryConfig<Channel>,
    ) = runCatching {
        searchScope.coroutineContext.cancelChildren()
        val queryChannelsRequest = QueryChannelsRequest(
            filter = config.filters,
            querySort = config.querySort,
            limit = channelLimit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
        )
        channelLoader.load(queryChannelsRequest)
    }.onFailure {
        when (it is CancellationException) {
            true -> logger.v { "[observeQueryChannels] cancelled" }
            else -> logger.e { "[observeQueryChannels] failed: $it" }
        }
    }

    internal fun loadMoreQueryChannels() {
        chListScope.launch(CoroutineExceptionHandler { _, throwable ->
            logger.e(throwable) {
                "failed to loadMoreQueryChannels"
            }
        }) { channelLoader.loadMore() }
    }
}
