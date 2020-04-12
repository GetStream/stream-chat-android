package io.getstream.chat.android.livedata.requests

import io.getstream.chat.android.client.api.models.*
import io.getstream.chat.android.client.utils.FilterObject


class QueryChannelPaginationRequest(var messageLimit: Int = 30) : ChannelQueryRequest() {

    var messageFilterDirection: Pagination? = null
    var messageFilterValue: String = ""

    var memberLimit: Int = 30
    var memberOffset: Int = 0

    var watcherLimit: Int = 30
    var watcherOffset: Int = 0

    fun hasFilter(): Boolean {
        return messageFilterDirection != null
    }

    fun isFirstPage(): Boolean {
        return messageFilterDirection == null
    }

    internal fun toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
        return AnyChannelPaginationRequest().apply {
            this.messageLimit=messageLimit
            this.messageFilterDirection=messageFilterDirection
            this.memberLimit=memberLimit
            this.memberOffset=memberOffset
            this.watcherLimit=watcherLimit
            this.watcherOffset=watcherOffset
            this.channelLimit=1
        }
    }

    fun toQueryChannelRequest(userPresence: Boolean): ChannelWatchRequest {
        var request = ChannelWatchRequest().withMessages(messageLimit)
        if (userPresence) {
            request = request.withPresence()
        }
        if (hasFilter()) {
            request.withMessages(messageFilterDirection!!, messageFilterValue, messageLimit)
        }
        return request
    }



    fun isFilteringNewerMessages(): Boolean {
        return (messageFilterDirection != null && (messageFilterDirection == Pagination.GREATER_THAN_OR_EQUAL || messageFilterDirection == Pagination.GREATER_THAN))
    }

    fun isFilteringOlderMessages(): Boolean {
        return (messageFilterDirection != null && (messageFilterDirection == Pagination.LESS_THAN || messageFilterDirection == Pagination.LESS_THAN_OR_EQUAL))
    }
}
