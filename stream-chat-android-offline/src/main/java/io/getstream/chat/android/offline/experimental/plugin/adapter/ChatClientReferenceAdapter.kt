package io.getstream.chat.android.offline.experimental.plugin.adapter

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.experimental.channel.QueryChannelReference
import io.getstream.chat.android.offline.experimental.channel.thread.RepliesQueryReference
import io.getstream.chat.android.offline.experimental.querychannels.QueryChannelsReference
import io.getstream.chat.android.offline.request.QueryChannelPaginationRequest

@InternalStreamChatApi
@ExperimentalStreamChatApi
/**
 * Adapter for [ChatClient] that wraps some it's request with [io.getstream.chat.android.offline.experimental.plugin.QueryReference].
 */
public class ChatClientReferenceAdapter(private val chatClient: ChatClient) {
    /** Reference request of the channels query. */
    public fun queryChannels(request: QueryChannelsRequest): QueryChannelsReference =
        QueryChannelsReference(request, chatClient)

    /** Reference request of the channel query. */
    public fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): QueryChannelReference = QueryChannelReference(channelType, channelId, request, chatClient)

    /** Reference request of the watch channel query. */
    public fun watchChannel(cid: String, limit: Int = DEFAULT_MESSAGE_LIMIT): QueryChannelReference {
        val (channelType, channelId) = cid.cidToTypeAndId()
        val userPresence = runCatching { ChatDomain.instance().userPresence }.getOrDefault(false)
        val request = QueryChannelPaginationRequest(limit).toWatchChannelRequest(userPresence)
        return queryChannel(channelType, channelId, request)
    }

    /** Reference request of the get thread replies query. */
    public fun getReplies(messageId: String, limit: Int = DEFAULT_MESSAGE_LIMIT): RepliesQueryReference =
        RepliesQueryReference(messageId, limit, chatClient)

    private companion object {
        private const val DEFAULT_MESSAGE_LIMIT = 30
    }
}
