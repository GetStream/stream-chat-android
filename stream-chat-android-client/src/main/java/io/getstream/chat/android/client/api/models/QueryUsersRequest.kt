package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.models.User

public data class QueryUsersRequest @JvmOverloads constructor(
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    var querySort: QuerySort<User> = QuerySort(),
    var presence: Boolean = false
) {
    val sort: List<Map<String, Any>> = querySort.toDto()
}
