package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject


data class QueryChannelsRequest(
    @IgnoreSerialisation
    val filter: FilterObject,
    var offset: Int,
    var limit: Int,
    val querySort: QuerySort = QuerySort(),
    @SerializedName("message_limit")
    var messageLimit: Int = 0
) : BaseQueryChannelRequest<QueryChannelsRequest>() {

    val sort = querySort.data
    val filter_conditions: Map<String, Any> = filter.getMap()

    fun withMessages(limit: Int): QueryChannelsRequest {
        return cloneOpts().apply { messageLimit = limit }
    }

    fun withLimit(limit: Int): QueryChannelsRequest {
        val clone = cloneOpts()
        clone.limit = limit
        return clone
    }

    fun withOffset(offset: Int): QueryChannelsRequest {
        return cloneOpts().apply { this.offset = offset }
    }

    init {
        this.watch = true
        this.state = true
        this.presence = false
    }

    override fun cloneOpts(): QueryChannelsRequest {
        val clone = QueryChannelsRequest(
            filter,
            offset,
            limit,
            querySort
        )
        clone.state = this.state
        clone.watch = this.watch
        clone.presence = this.presence
        clone.messageLimit = messageLimit
        return clone
    }
}
