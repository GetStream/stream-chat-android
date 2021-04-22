package io.getstream.chat.android.offline.request

import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.api.models.WatchChannelRequest

internal class QueryChannelPaginationRequest(var messageLimit: Int = 30) : QueryChannelRequest() {

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
        val originalRequest = this
        return AnyChannelPaginationRequest().apply {
            this.messageLimit = originalRequest.messageLimit
            this.messageFilterDirection = originalRequest.messageFilterDirection
            this.memberLimit = originalRequest.memberLimit
            this.memberOffset = originalRequest.memberOffset
            this.watcherLimit = originalRequest.watcherLimit
            this.watcherOffset = originalRequest.watcherOffset
            this.channelLimit = 1
        }
    }

    fun toQueryChannelRequest(userPresence: Boolean): WatchChannelRequest {
        var request = WatchChannelRequest().withMessages(messageLimit)
        if (userPresence) {
            // TODO: clean up once LLC is fixed
            request = request.withPresence() as WatchChannelRequest
        }
        if (hasFilter()) {
            request.withMessages(messageFilterDirection!!, messageFilterValue, messageLimit)
        }
        return request
    }
}
