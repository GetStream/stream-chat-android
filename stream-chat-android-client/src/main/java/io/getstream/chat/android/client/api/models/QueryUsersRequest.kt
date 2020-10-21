package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject

public data class QueryUsersRequest @JvmOverloads constructor(
    @IgnoreSerialisation
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    @IgnoreSerialisation
    var querySort: QuerySort = QuerySort(),
    var presence: Boolean = false
) {
    val sort: MutableList<Map<String, Any>> = querySort.data
    val filter_conditions: Map<String, Any> = filter.toMap()
}
