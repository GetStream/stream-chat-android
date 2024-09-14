package io.getstream.chat.android.compose.viewmodel.channels.filtering

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.viewmodel.channels.IChannelViewState
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.uiutils.extension.defaultChannelListFilter
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

internal class StreamChannelFilter(
    private val chatClient: ChatClient,
    channelViewState: IChannelViewState,
) :
    IFilterChannels, IChannelViewState by channelViewState {
    private val logger by taggedLogger("Chat:ChannelListVM:StreamChannelFilter")

    /**
     * Builds the default channel filter, which represents "messaging" channels that the current user is a part of.
     */
    private fun buildDefaultFilter(): Flow<FilterObject> {
        return chatClient.clientState.user.map(Filters::defaultChannelListFilter).filterNotNull()
    }

    override fun setupFilters(initialFilters: FilterObject?, viewModelScope: CoroutineScope) {
        if (initialFilters == null) {
            viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
                logger.e(throwable) {
                    "Failed to setup filters"
                }
            }) {
                val filter = buildDefaultFilter().first()
                filterFlow.value = filter
            }
        }
    }
}