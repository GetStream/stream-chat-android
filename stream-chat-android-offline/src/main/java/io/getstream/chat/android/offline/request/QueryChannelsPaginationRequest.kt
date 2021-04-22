package io.getstream.chat.android.offline.request

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

internal data class QueryChannelsPaginationRequest(
    val sort: QuerySort<Channel>,
    val channelOffset: Int = 0,
    val channelLimit: Int = 30,
    val messageLimit: Int = 10,
    val memberLimit: Int
) {

    val isFirstPage: Boolean
        get() = channelOffset == 0
}
