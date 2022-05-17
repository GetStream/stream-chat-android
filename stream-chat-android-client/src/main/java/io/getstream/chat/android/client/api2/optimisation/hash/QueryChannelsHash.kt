package io.getstream.chat.android.client.api2.optimisation.hash

import io.getstream.chat.android.client.api.models.QueryChannelRequest

internal data class QueryChannelsHash(
    val channelType: String? = null,
    val channelId: String? = null,
    val query: QueryChannelRequest,
)
