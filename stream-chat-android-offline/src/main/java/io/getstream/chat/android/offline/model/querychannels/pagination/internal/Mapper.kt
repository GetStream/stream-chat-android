package io.getstream.chat.android.offline.model.querychannels.pagination.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelRequest
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

internal fun QueryChannelRequest.toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
    val originalRequest = this
    val paginationAndValue = pagination()
    return AnyChannelPaginationRequest().apply {
        this.messageLimit = originalRequest.messagesLimit()
        this.messageFilterDirection = paginationAndValue?.first
        this.messageFilterValue = paginationAndValue?.second ?: ""
        this.memberLimit = originalRequest.membersLimit()
        this.memberOffset = originalRequest.membersOffset()
        this.watcherLimit = originalRequest.watchersLimit()
        this.watcherOffset = originalRequest.watchersOffset()
        this.channelLimit = 1
    }
}
