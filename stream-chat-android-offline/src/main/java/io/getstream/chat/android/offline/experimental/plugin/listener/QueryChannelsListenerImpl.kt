package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry

/**
 * Document this!!
 */
internal class QueryChannelsListenerImpl(private val logic: LogicRegistry) : QueryChannelsListener {

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        logic.queryChannels(request).onQueryChannelsRequest(request)
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        logic.queryChannels(request).onQueryChannelsResult(result, request)
    }
}
