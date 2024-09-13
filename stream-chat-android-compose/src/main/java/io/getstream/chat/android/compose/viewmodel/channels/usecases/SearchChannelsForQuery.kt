package io.getstream.chat.android.compose.viewmodel.channels.usecases

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.compose.state.QueryConfig
import io.getstream.chat.android.compose.viewmodel.channels.CreateQueryChannelsFilter
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.extensions.queryChannelsAsState
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.log.TaggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

internal class SearchChannelsForQuery(
    private val channelLimit: Int,
    private val messageLimit: Int,
    private val memberLimit: Int,
    private val chatClient: ChatClient,
    private val chatEventHandlerFactory: ChatEventHandlerFactory,
    private val channelState: IChannelViewState,
    private val logger: TaggedLogger,
) : IChannelViewState by channelState {
    private var lastNextQuery: QueryChannelsRequest? = null

    internal suspend operator fun invoke(
        searchScope: CoroutineScope,
        chListScope: CoroutineScope,
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

        logger.d { "[observeQueryChannels] request: $queryChannelsRequest" }
        queryChannelsState = chatClient.queryChannelsAsState(
            request = queryChannelsRequest,
            chatEventHandlerFactory = chatEventHandlerFactory,
            coroutineScope = chListScope,
        )

        queryChannelsState.filterNotNull().collectLatest { queryChannelsState ->
            channelMutes.combine(queryChannelsState.channelsStateData, ::Pair).map { (channelMutes, state) ->
                channelsState(
                    state = state,
                    channelMutes = channelMutes,
                    queryChannelsState = queryChannelsState
                )
            }.collectLatest { newState -> channelsState = newState }
        }
    }.onFailure {
        when (it is CancellationException) {
            true -> logger.v { "[observeQueryChannels] cancelled" }
            else -> logger.e { "[observeQueryChannels] failed: $it" }
        }
    }

    private fun channelsState(
        state: ChannelsStateData,
        channelMutes: List<ChannelMute>,
        queryChannelsState: QueryChannelsState,
    ) = when (state) {
        ChannelsStateData.NoQueryActive,
        ChannelsStateData.Loading,
        -> channelsState.copy(
            isLoading = true,
            searchQuery = searchQuery.value,
        ).also {
            logger.d { "[observeQueryChannels] state: Loading" }
        }

        ChannelsStateData.OfflineNoResults -> {
            logger.v { "[observeQueryChannels] state: OfflineNoResults(channels are empty)" }
            channelsState.copy(
                isLoading = false,
                channelItems = emptyList(),
                searchQuery = searchQuery.value,
            )
        }

        is ChannelsStateData.Result -> {
            logger.v { "[observeQueryChannels] state: Result(channels.size: ${state.channels.size})" }
            channelsState.copy(
                isLoading = false,
                channelItems = CreateChannelItems()(state.channels, channelMutes),
                isLoadingMore = false,
                endOfChannels = queryChannelsState.endOfChannels.value,
                searchQuery = searchQuery.value,
            )
        }
    }

    internal suspend fun loadMoreQueryChannels() {
        logger.d { "[loadMoreQueryChannels] no args" }
        val currentFilter = filterFlow.value
        if (currentFilter == null) {
            logger.v { "[loadMoreQueryChannels] rejected (no current filter)" }
            return
        }
        val currentQuery = queryChannelsState.value?.nextPageRequest?.value
        if (currentQuery == null) {
            logger.v { "[loadMoreQueryChannels] rejected (no current query)" }
            return
        }
        if (channelsState.endOfChannels) {
            logger.v { "[loadMoreQueryChannels] rejected (end of channels)" }
            return
        }
        if (channelsState.isLoadingMore) {
            logger.v { "[loadMoreQueryChannels] rejected (already loading more)" }
            return
        }
        val nextQuery = currentQuery.copy(
            filter = CreateQueryChannelsFilter()(currentFilter, searchQuery.value.query),
            querySort = querySortFlow.value,
        )
        if (lastNextQuery == nextQuery) {
            logger.v { "[loadMoreQueryChannels] rejected (same query)" }
            return
        }
        lastNextQuery = nextQuery
        logger.v { "[loadMoreQueryChannels] offset: ${nextQuery.offset}, limit: ${nextQuery.limit}" }
        channelsState = channelsState.copy(isLoadingMore = true)
        val result = chatClient.queryChannels(nextQuery).await()
        if (result.isSuccess) {
            logger.v { "[loadMoreQueryChannels] completed; channels.size: ${result.getOrNull()?.size}" }
        } else {
            logger.e { "[loadMoreQueryChannels] failed: ${result.errorOrNull()}" }
        }
        channelsState = channelsState.copy(isLoadingMore = false)
    }
}
