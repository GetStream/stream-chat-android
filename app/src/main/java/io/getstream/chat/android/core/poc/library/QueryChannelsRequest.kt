package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.requests.QuerySort


class QueryChannelsRequest(
    val filter: FilterObject,
    val querySort: QuerySort,
    var message_limit: Int = 0,
    var limit: Int = 0,
    var offset: Int = 0
) : BaseQueryChannelRequest<QueryChannelsRequest>() {

    val sort = querySort.data

    fun query(): QueryChannelsQ {
        return QueryChannelsQ(filter, querySort)
    }

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
        val _this = QueryChannelsRequest(filter, querySort)
        _this.state = this.state
        _this.watch = this.watch
        _this.limit = limit
        _this.offset = offset
        _this.presence = this.presence
        _this.message_limit = message_limit
        return _this
    }
}
