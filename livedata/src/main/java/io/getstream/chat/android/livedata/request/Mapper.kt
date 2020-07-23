package io.getstream.chat.android.livedata.request

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.utils.FilterObject

internal fun QueryChannelsPaginationRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest =
    AnyChannelPaginationRequest().apply {
        this.channelLimit = channelLimit
        this.channelOffset = channelOffset
        this.messageLimit = messageLimit
    }

internal fun QueryChannelsPaginationRequest.toQueryChannelsRequest(filter: FilterObject, userPresence: Boolean): QueryChannelsRequest {
    var request = QueryChannelsRequest(filter, channelOffset, channelLimit, sort)

    request = request.withMessages(messageLimit)
    if (userPresence) {
        request = request.withPresence()
    }
    return request.withWatch()
}
