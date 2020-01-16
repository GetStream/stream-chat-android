package io.getstream.chat.android.core.poc.library

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.requests.QuerySort


class QueryChannelsRequest(


    val filter: FilterObject,
    val sort: QuerySort
) :
    BaseQueryChannelRequest<QueryChannelsRequest>() {


    @Expose
    @SerializedName("message_limit")
    var messageLimit = 0
    @Expose
    var limit = 0
    @Expose
    var offset = 0

    @Expose
    @SerializedName("sort")
    val sortList = sort.data

    fun query(): QueryChannelsQ {
        return QueryChannelsQ(filter, sort)
    }

    fun withMessageLimit(limit: Int): QueryChannelsRequest {
        val clone = cloneOpts()
        clone.messageLimit = limit
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
        val _this = QueryChannelsRequest(filter, sort)
        _this.state = this.state
        _this.watch = this.watch
        _this.limit = limit
        _this.offset = offset
        _this.presence = this.presence
        _this.messageLimit = messageLimit
        return _this
    }
}
