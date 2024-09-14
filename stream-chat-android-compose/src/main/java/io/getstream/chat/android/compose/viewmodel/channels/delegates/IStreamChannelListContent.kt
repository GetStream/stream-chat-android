package io.getstream.chat.android.compose.viewmodel.channels.delegates

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.compose.viewmodel.channels.getConfig
import io.getstream.chat.android.compose.viewmodel.channels.loadchannels.SearchChannelsForQuery
import io.getstream.chat.android.compose.viewmodel.channels.loadmessages.SearchMessagesForQuery
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.mapLatest

internal interface IStreamChannelListContent {
    fun streamSearchQuery(): Flow<Any>
    fun loadMore()
}

internal class StreamChannelListContentLoader(
    private val chatClient: ChatClient,
    chatEventHandlerFactory: ChatEventHandlerFactory,
    channelLimit: Int,
    memberLimit: Int,
    messageLimit: Int,
    channelViewState: IChannelViewState,
    searchDebounceMs: Long,
    coroutineScope: CoroutineScope,
) : IStreamChannelListContent, IChannelViewState by channelViewState {
    private val logger by taggedLogger("Chat:ChannelListVM:StreamChannelListContentLoader")

    /**
     *  A Search Helper delegates the ownership for the searchScope and debouncer
     */
    private val streamSearchHelper by lazy {
        StreamChannelSearchHelper(
            searchDebounceMs = searchDebounceMs,
            viewModelScope = coroutineScope
        )
    }
    /**
     *  A SearchChannelsForQuery uses StreamChannelLoader to load for initial query and help load more
     */
    private val searchChannelsForQuery by lazy {
        SearchChannelsForQuery(
            channelLimit = channelLimit,
            messageLimit = messageLimit,
            memberLimit = memberLimit,
            chatClient = chatClient,
            chatEventHandlerFactory = chatEventHandlerFactory,
            channelState = this,
            logger = logger,
            iHelpSearchWithDebounce = streamSearchHelper,
        )
    }

    /**
     *  A searchMessagesForQuery uses StreamMessagesLoader to load for initial query and help load more
     */
    private val searchMessagesForQuery by lazy {
        SearchMessagesForQuery(
            chatClient = chatClient,
            logger = logger,
            channelLimit = channelLimit,
            channelState = this,
            iHelpSearchWithDebounce = streamSearchHelper,
        )
    }

    /**
     * Makes the initial query to request channels and starts observing state changes.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun streamSearchQuery(): Flow<Any> {
        return searchQuery.combine(queryConfigFlow) { query, config -> query to config }
            .mapLatest { (query, config) ->
                when (query) {
                    is SearchQuery.Empty,
                    is SearchQuery.Channels,
                    -> {
                        searchChannelsForQuery(
                            config = query.getConfig(config),
                        )
                    }

                    is SearchQuery.Messages -> {
                        searchMessagesForQuery(
                            query = query.query,
                        )
                    }
                }
            }.catch {
                logger.e(it) {
                    "setupSearchAndQuery failed"
                }
            }
    }

    override fun loadMore() {
        if (chatClient.clientState.isOffline) {
            logger.v { "[loadMore] rejected (client is offline)" }
            return
        }
        when (searchQuery.value) {
            is SearchQuery.Empty,
            is SearchQuery.Channels,
            -> searchChannelsForQuery.loadMoreQueryChannels()

            is SearchQuery.Messages,
            -> searchMessagesForQuery.loadMoreQueryMessages()
        }
    }
}