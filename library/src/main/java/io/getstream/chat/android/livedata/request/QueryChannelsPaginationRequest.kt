package io.getstream.chat.android.livedata.request

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.utils.FilterObject

/**
 * Paginate query channels on the queryChannels repo
 * Similar to QueryChannelsRequest but without the watch, filter and sort params
 * Since those are provided by the QueryChannelsRepo
 */
data class QueryChannelsPaginationRequest(
    var channelOffset: Int = 0,
    var channelLimit: Int = 30,
    var messageLimit: Int = 10
) {

    fun isFirstPage(): Boolean {
        return channelOffset == 0
    }

    internal fun toAnyChannelPaginationRequest(): AnyChannelPaginationRequest {
        return AnyChannelPaginationRequest().apply {
            this.channelLimit = channelLimit
            this.channelOffset = channelOffset
            this.messageLimit = messageLimit
        }
    }

    fun toQueryChannelsRequest(filter: FilterObject, sort: QuerySort?, userPresence: Boolean): QueryChannelsRequest {
        val querySort = sort ?: QuerySort()
        var request = QueryChannelsRequest(filter, channelOffset, channelLimit, querySort)

        request = request.withMessages(messageLimit)
        if (userPresence) {
            request = request.withPresence()
        }
        return request.withWatch()
    }
}
