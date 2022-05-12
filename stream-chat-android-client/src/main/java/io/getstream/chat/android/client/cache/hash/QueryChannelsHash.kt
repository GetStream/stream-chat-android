package io.getstream.chat.android.client.cache.hash

import io.getstream.chat.android.client.api.models.QueryChannelRequest

internal data class QueryChannelsHash(
    val channelType: String? = null,
    val channelId: String? = null,
    val query: QueryChannelRequest,
)
