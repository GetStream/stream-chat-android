package io.getstream.chat.android.client.helpers

import io.getstream.chat.android.client.api.ChatApi
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.map
import io.getstream.chat.android.client.clientstate.ClientStateService
import io.getstream.chat.android.client.models.Channel

internal class QueryChannelsPostponeHelper(
    private val api: ChatApi,
    private val clientStateService: ClientStateService
) {

    internal fun queryChannel(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest
    ): Call<Channel> {
        // for convenience we add the message.cid field
        return api.queryChannel(channelType, channelId, request)
            .map { channel ->
                channel.messages.forEach { message -> message.cid = channel.cid }
                channel
            }
    }

    internal fun queryChannels(request: QueryChannelsRequest): Call<List<Channel>> {
        return api.queryChannels(request).map { channels ->
            channels.map { channel ->
                channel.messages.forEach { message ->
                    message.cid = channel.cid
                }
            }
            channels
        }
    }
}