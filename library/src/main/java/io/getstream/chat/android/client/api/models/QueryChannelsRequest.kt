package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject

class QueryChannelsRequest(
    @IgnoreSerialisation val filter: FilterObject,
    var offset: Int,
    var limit: Int,
    @IgnoreSerialisation val querySort: QuerySort = QuerySort(),
    @SerializedName("message_limit") var messageLimit: Int = 0,
    @SerializedName("member_limit") var memberLimit: Int = 0
) : ChannelRequest<QueryChannelsRequest> {

    override var state: Boolean = true
    override var watch: Boolean = true
    override var presence: Boolean = false

    val sort = querySort.data
    val filter_conditions: Map<String, Any> = filter.toMap()

    fun withMessages(limit: Int): QueryChannelsRequest {
        messageLimit = limit
        return this
    }

    fun withLimit(limit: Int): QueryChannelsRequest {
        this.limit = limit
        return this
    }

    fun withOffset(offset: Int): QueryChannelsRequest {
        this.offset = offset
        return this
    }
}
