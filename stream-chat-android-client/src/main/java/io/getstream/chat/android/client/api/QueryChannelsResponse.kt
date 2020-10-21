package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.api.models.ChannelResponse

internal data class QueryChannelsResponse(
    var channels: List<ChannelResponse> = emptyList()
)
