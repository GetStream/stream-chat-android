package io.getstream.chat.android.livedata.request

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QuerySort

internal class AnyChannelPaginationRequest(var messageLimit: Int = 30) {
    var messageFilterDirection: Pagination? = null
    var messageFilterValue: String = ""
    var sort: QuerySort = QuerySort()

    var channelLimit: Int = 30
    var channelOffset: Int = 0

    var memberLimit: Int = 30
    var memberOffset: Int = 0

    var watcherLimit: Int = 30
    var watcherOffset: Int = 0

    fun setFilter(messageFilterDirection: Pagination, messageFilterValue: String) {
        this.messageFilterDirection = messageFilterDirection
        this.messageFilterValue = messageFilterValue
    }

    fun hasFilter(): Boolean {
        return messageFilterDirection != null
    }

    fun isFirstPage(): Boolean {
        return messageFilterDirection == null
    }

    fun isRequestingMoreThanLastMessage(): Boolean {
        return (isFirstPage() && messageLimit > 1) || (isNotFirstPage() && messageLimit > 0)
    }

    fun isFilteringNewerMessages(): Boolean {
        return (messageFilterDirection != null && (messageFilterDirection == Pagination.GREATER_THAN_OR_EQUAL || messageFilterDirection == Pagination.GREATER_THAN))
    }

    fun isFilteringOlderMessages(): Boolean {
        return (messageFilterDirection != null && (messageFilterDirection == Pagination.LESS_THAN || messageFilterDirection == Pagination.LESS_THAN_OR_EQUAL))
    }

    fun isNotFirstPage(): Boolean {
        return !isFirstPage()
    }
}
