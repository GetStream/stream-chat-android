package io.getstream.chat.android.client

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.doOnResult
import io.getstream.chat.android.client.call.doOnStart
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.CoroutineScope

public interface QueryReference<T : Any> {
    public fun get(): Call<T>
}

public class QueryChannelsReference(
    public val request: QueryChannelsRequest,
    private val chatClient: ChatClient,
    private val scope: CoroutineScope,
) : QueryReference<List<Channel>> {
    override fun get(): Call<List<Channel>> {
        return chatClient.queryChannelsPostponeHelper.queryChannels(request)
            .doOnStart(scope) {
                chatClient.plugins.forEach { it.onQueryChannelsRequest(request) }
            }
            .doOnResult(scope) { result ->
                chatClient.plugins.forEach { it.onQueryChannelsResult(result, request) }
            }
    }
}
