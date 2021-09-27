package io.getstream.chat.android.offline.request

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

internal class AnyChannelPaginationRequest(var messageLimit: Int = 30) {
    var messageFilterDirection: Pagination? = null
    var messageFilterValue: String = ""
    var sort: QuerySort<Channel> = QuerySort()

    var channelLimit: Int = 30
    var channelOffset: Int = 0

    var memberLimit: Int = 30
    var memberOffset: Int = 0

    var watcherLimit: Int = 30
    var watcherOffset: Int = 0
}

internal fun AnyChannelPaginationRequest.hasFilter(): Boolean {
    return messageFilterDirection != null
}

internal fun AnyChannelPaginationRequest.isFirstPage(): Boolean {
    return channelOffset == 0
}

internal fun AnyChannelPaginationRequest.isRequestingMoreThanLastMessage(): Boolean {
    return (isFirstPage() && messageLimit > 1) || (isNotFirstPage() && messageLimit > 0)
}

internal fun AnyChannelPaginationRequest.isNotFirstPage(): Boolean = isFirstPage().not()
