package io.getstream.chat.android.offline.request

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest

internal fun QueryChannelsPaginationRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    return AnyChannelPaginationRequest().apply {
        this.channelLimit = originalRequest.channelLimit
        this.channelOffset = originalRequest.channelOffset
        this.messageLimit = originalRequest.messageLimit
        this.sort = originalRequest.sort
    }
}

internal fun QueryChannelsPaginationRequest.toQueryChannelsRequest(filter: FilterObject, userPresence: Boolean): QueryChannelsRequest {
    var request = QueryChannelsRequest(filter, channelOffset, channelLimit, sort, messageLimit, memberLimit)
    if (userPresence) {
        request = request.withPresence()
    }
    return request.withWatch()
}
