package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.Member

internal data class QueryMembersRequest(
    val channelType: String,
    val channelId: String,
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    var querySort: QuerySort<Member> = QuerySort(),
    val members: List<Member> = emptyList()
) {
    val sort: List<Map<String, Any>> = querySort.toDto()
}
