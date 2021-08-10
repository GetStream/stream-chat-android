package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.extensions.state
import io.getstream.chat.android.offline.plugin.QueryReference
import io.getstream.chat.android.offline.querychannels.state.QueryChannelsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public class QueryChannelsReference(
    public val request: QueryChannelsRequest,
    private val chatClient: ChatClient
) : QueryReference<List<Channel>, QueryChannelsState> {
    override fun get(): Call<List<Channel>> {
        return chatClient.queryChannels(request)
    }

    override fun asState(scope: CoroutineScope): QueryChannelsState {
        scope.launch {
            get().await()
        }

        return chatClient.state.queryChannels(request.filter, request.querySort)
    }
}
