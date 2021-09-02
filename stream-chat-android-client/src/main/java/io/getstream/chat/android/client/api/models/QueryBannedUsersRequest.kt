package io.getstream.chat.android.client.api.models

import java.util.Date

internal data class QueryBannedUsersRequest(
    var filter: FilterObject,
    val sort: List<Map<String, Any>>,
    val offset: Int?,
    val limit: Int?,
    val createdAtAfter: Date?,
    val createdAtAfterOrEqual: Date?,
    val createdAtBefore: Date?,
    val createdAtBeforeOrEqual: Date?,
)
