package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Channel

public data class QueryChannelsRequest(
    public val filter: FilterObject,
    public var offset: Int = 0,
    public var limit: Int,
    public val querySort: QuerySort<Channel> = QuerySort(),
    public var messageLimit: Int = 0,
    public var memberLimit: Int = 0
) : ChannelRequest<QueryChannelsRequest> {

    override var state: Boolean = true
    override var watch: Boolean = true
    override var presence: Boolean = false

    public val sort: List<Map<String, Any>> = querySort.toDto()

    public fun withMessages(limit: Int): QueryChannelsRequest {
        messageLimit = limit
        return this
    }

    public fun withLimit(limit: Int): QueryChannelsRequest {
        this.limit = limit
        return this
    }

    public fun withOffset(offset: Int): QueryChannelsRequest {
        this.offset = offset
        return this
    }
}
