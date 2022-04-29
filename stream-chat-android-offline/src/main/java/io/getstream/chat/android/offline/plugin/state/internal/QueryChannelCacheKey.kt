package io.getstream.chat.android.offline.plugin.state.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest

public data class QueryChannelCacheKey(
    val request: QueryChannelsRequest,
    val channelType: String? = null,
    val channelId: String? = null,
)
