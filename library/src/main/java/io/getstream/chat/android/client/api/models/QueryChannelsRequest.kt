package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.utils.FilterObject


data class QueryChannelsRequest(
    var offset: Int = 0,
    var limit: Int = 0,
    val filter: FilterObject = FilterObject(),
    val querySort: QuerySort = QuerySort(),
    var message_limit: Int = 0
) : BaseQueryChannelRequest<QueryChannelsRequest>() {

    val sort = querySort.data

    fun withMessageLimit(limit: Int): QueryChannelsRequest {
        val clone = cloneOpts()
        return clone
    }

    fun withLimit(limit: Int): QueryChannelsRequest {
        val clone = cloneOpts()
        clone.limit = limit
        return clone
    }

    fun withOffset(offset: Int): QueryChannelsRequest {
        val clone = cloneOpts()
        clone.offset = offset
        return clone
    }

    init {
        this.watch = true
        this.state = true
        this.presence = false
    }

    override fun cloneOpts(): QueryChannelsRequest {
        val _this = QueryChannelsRequest(
            offset,
            limit,
            filter,
            querySort
        )
        _this.state = this.state
        _this.watch = this.watch
        _this.presence = this.presence
        _this.message_limit = message_limit
        return _this
    }
}
