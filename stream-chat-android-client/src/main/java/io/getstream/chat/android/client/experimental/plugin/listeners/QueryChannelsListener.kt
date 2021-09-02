package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
public interface QueryChannelsListener {
    public suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {}

    public suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {}
}
