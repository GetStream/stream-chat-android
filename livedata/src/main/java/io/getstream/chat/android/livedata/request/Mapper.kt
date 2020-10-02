package io.getstream.chat.android.livedata.request

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.utils.FilterObject

internal fun QueryChannelsPaginationRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest =
    AnyChannelPaginationRequest().apply {
        this.channelLimit = channelLimit
        this.channelOffset = channelOffset
        this.messageLimit = messageLimit
        this.sort = sort
    }

internal fun QueryChannelsPaginationRequest.toQueryChannelsRequest(filter: FilterObject, userPresence: Boolean): QueryChannelsRequest {
    var request = QueryChannelsRequest(filter, channelOffset, channelLimit, sort, messageLimit, memberLimit)
    if (userPresence) {
        request = request.withPresence()
    }
    return request.withWatch()
}
