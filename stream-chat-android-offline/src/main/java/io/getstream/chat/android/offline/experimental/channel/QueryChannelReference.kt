package io.getstream.chat.android.offline.experimental.channel

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.extensions.state
import io.getstream.chat.android.offline.experimental.plugin.query.QueryReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@InternalStreamChatApi
/** Reference for the [ChatClient.queryChannel] request. */
public class QueryChannelReference internal constructor(
    private val channelType: String,
    private val channelId: String,
    private val request: QueryChannelRequest,
    private val chatClient: ChatClient,
) : QueryReference<Channel, ChannelState> {

    /** Returns a call of the channel type representing output value of [ChatClient.queryChannel]. */
    override fun get(): Call<Channel> {
        return chatClient.queryChannel(channelType, channelId, request)
    }

    /** Returns [ChannelState] for combination of [channelType] and [channelId]. And fill it by data from
     * [ChatClient.queryChannel].
     *
     * @param scope Coroutine scope where initial data filling action is being invoked.
     */
    override fun asState(scope: CoroutineScope): ChannelState {
        scope.launch {
            get().await()
        }
        return chatClient.state.channel(channelType, channelId)
    }
}
