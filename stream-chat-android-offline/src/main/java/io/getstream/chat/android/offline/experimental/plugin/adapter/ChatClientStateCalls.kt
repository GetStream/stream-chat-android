package io.getstream.chat.android.offline.experimental.plugin.adapter

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.launch
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadState
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsState
import io.getstream.chat.android.offline.request.QueryChannelPaginationRequest
import kotlinx.coroutines.CoroutineScope

/**
 * Adapter for [ChatClient] that wraps some of it's request with [io.getstream.chat.android.offline.experimental.plugin.QueryReference].
 */
internal class ChatClientStateCalls(
    private val chatClient: ChatClient,
    private val state: StateRegistry,
    private val scope: CoroutineScope
) {
    /** Reference request of the channels query. */
    internal fun queryChannels(request: QueryChannelsRequest): QueryChannelsState {
        chatClient.queryChannels(request).launch(scope)
        return state.queryChannels(request.filter, request.querySort)
    }

    /** Reference request of the channel query. */
    private fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): ChannelState {
        chatClient.queryChannel(channelType, channelId, request).launch(scope)
        return state.channel(channelType, channelId)
    }

    /** Reference request of the watch channel query. */
    internal fun watchChannel(cid: String, limit: Int): ChannelState {
        val (channelType, channelId) = cid.cidToTypeAndId()
        val userPresence = true // todo: Fix this!!
        val request = QueryChannelPaginationRequest(limit).toWatchChannelRequest(userPresence)
        return queryChannel(channelType, channelId, request)
    }

    /** Reference request of the get thread replies query. */
    internal fun getReplies(messageId: String, limit: Int): ThreadState {
        chatClient.getReplies(messageId, limit).launch(scope)
        return state.thread(messageId)
    }
}
