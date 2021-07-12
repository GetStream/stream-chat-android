package io.getstream.chat.android.client.offline.request

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public class AnyChannelPaginationRequest(public var messageLimit: Int = 30) {
    public var messageFilterDirection: Pagination? = null
    public var messageFilterValue: String = ""
    public var sort: QuerySort<Channel> = QuerySort()

    public var channelLimit: Int = 30
    public var channelOffset: Int = 0

    public var memberLimit: Int = 30
    public var memberOffset: Int = 0

    public var watcherLimit: Int = 30
    public var watcherOffset: Int = 0
}

internal fun AnyChannelPaginationRequest.hasFilter(): Boolean {
    return messageFilterDirection != null
}

internal fun AnyChannelPaginationRequest.isFirstPage(): Boolean {
    return messageFilterDirection == null
}

internal fun AnyChannelPaginationRequest.isRequestingMoreThanLastMessage(): Boolean {
    return (isFirstPage() && messageLimit > 1) || (isNotFirstPage() && messageLimit > 0)
}

internal fun AnyChannelPaginationRequest.isNotFirstPage(): Boolean {
    return !isFirstPage()
}

internal fun AnyChannelPaginationRequest.isFilteringNewerMessages(): Boolean {
    return (messageFilterDirection != null && (messageFilterDirection == Pagination.GREATER_THAN_OR_EQUAL || messageFilterDirection == Pagination.GREATER_THAN))
}

internal fun AnyChannelPaginationRequest.isFilteringOlderMessages(): Boolean {
    return (messageFilterDirection != null && (messageFilterDirection == Pagination.LESS_THAN || messageFilterDirection == Pagination.LESS_THAN_OR_EQUAL))
}
