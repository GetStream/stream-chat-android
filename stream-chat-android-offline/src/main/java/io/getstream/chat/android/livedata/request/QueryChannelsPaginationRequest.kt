package io.getstream.chat.android.livedata.request

import io.getstream.chat.android.client.api.models.QuerySort

internal data class QueryChannelsPaginationRequest(
    val sort: QuerySort,
    val channelOffset: Int = 0,
    val channelLimit: Int = 30,
    val messageLimit: Int = 10,
    val memberLimit: Int
) {

    val isFirstPage: Boolean
        get() = channelOffset == 0
}
