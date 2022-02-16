package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelListener
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

@ExperimentalStreamChatApi
internal class QueryChannelListenerImpl(private val logic: LogicRegistry) : QueryChannelListener {

    override suspend fun onQueryChannelPrecondition(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ): Result<Unit> =
        logic.channel(channelType, channelId).onQueryChannelPrecondition(channelType, channelId, request)

    override suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        logic.channel(channelType, channelId).onQueryChannelRequest(channelType, channelId, request)
    }

    override suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {
        logic.channel(channelType, channelId).onQueryChannelResult(result, channelType, channelId, request)
    }
}
