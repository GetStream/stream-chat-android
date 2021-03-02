package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.parser.IgnoreSerialisation

internal data class QueryMembersRequest(
    @SerializedName("type")
    val channelType: String,
    @SerializedName("id")
    val channelId: String,
    @SerializedName("filter_conditions")
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    @IgnoreSerialisation
    var querySort: QuerySort<Member> = QuerySort(),
    val members: List<Member> = emptyList()
) {
    val sort: List<Map<String, Any>> = querySort.toDto()
}
