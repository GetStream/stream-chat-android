package io.getstream.chat.android.client.api.models

internal data class QueryChannelsResponse(
    var channels: List<ChannelResponse> = emptyList()
)
