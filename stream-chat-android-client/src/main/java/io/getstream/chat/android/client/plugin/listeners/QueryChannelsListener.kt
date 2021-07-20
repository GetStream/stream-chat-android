package io.getstream.chat.android.client.plugin.listeners

import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result

public interface QueryChannelsListener {
    public suspend fun onQueryChannelRequest(
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {}

    public suspend fun onQueryChannelResult(
        result: Result<Channel>,
        channelType: String,
        channelId: String,
        request: QueryChannelRequest,
    ) {}

    public suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {}

    public suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {}
}
