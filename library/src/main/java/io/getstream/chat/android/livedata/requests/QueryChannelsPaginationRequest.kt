package io.getstream.chat.android.livedata.requests


import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.api.models.BaseQueryChannelRequest
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject

/**
 * Paginate query channels on the queryChannels repo
 * Similar to QueryChannelsRequest but without the watch, filter and sort params
 * Since those are provided by the QueryChannelsRepo
 */
data class QueryChannelsPaginationRequest(
    var offset: Int = 0,
    var limit: Int = 30,
    var messageLimit: Int = 10
) : BaseQueryChannelRequest<QueryChannelsPaginationRequest>() {

    fun isFirstPage(): Boolean {
        return offset == 0
    }

    fun withMessages(limit: Int): QueryChannelsPaginationRequest {
        return cloneOpts().apply { messageLimit = limit }
    }

    fun withLimit(limit: Int): QueryChannelsPaginationRequest {
        val clone = cloneOpts()
        clone.limit = limit
        return clone
    }

    fun withOffset(offset: Int): QueryChannelsPaginationRequest {
        return cloneOpts().apply { this.offset = offset }
    }

    fun toQueryChannelsRequest(filter: FilterObject, sort: QuerySort?, presence: Boolean): QueryChannelsRequest {
        val querySort = sort ?: QuerySort()
        var request = QueryChannelsRequest(filter, offset, limit, querySort)

        request = request.withMessages(messageLimit)
        if (presence) {
            request = request.withPresence()
        }
        return request.withWatch()
    }

    override fun cloneOpts(): QueryChannelsPaginationRequest {
        val clone = QueryChannelsPaginationRequest(
            offset,
            limit,
            messageLimit
        )
        clone.presence = this.presence
        return clone
    }
}
