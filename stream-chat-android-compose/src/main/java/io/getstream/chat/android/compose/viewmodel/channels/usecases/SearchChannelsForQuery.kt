package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.compose.viewmodel.channels.delegates.ILoadChannels
import io.getstream.chat.android.compose.viewmodel.channels.delegates.StreamChannelLoader
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlin.coroutines.cancellation.CancellationException

internal class SearchChannelsForQuery(
    private val channelLimit: Int,
    private val messageLimit: Int,
    private val memberLimit: Int,
    chatClient: ChatClient,
    chatEventHandlerFactory: ChatEventHandlerFactory,
    private val channelState: IChannelViewState,
    private val logger: TaggedLogger,
    chListScope: CoroutineScope,
) : IChannelViewState by channelState {


    private val channelLoader: ILoadChannels = StreamChannelLoader(
        chatClient,
        chatEventHandlerFactory,
        chListScope,
        channelState,
        logger
    )

    internal suspend operator fun invoke(
        searchScope: CoroutineScope,
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

    internal suspend fun loadMoreQueryChannels() {
        channelLoader.loadMore()
    }
}
