package io.getstream.chat.android.client.cache.hash

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.BannedUsersSort
import java.util.Date

internal data class QueryBanedUsersHash(
    val filter: FilterObject,
    val sort: QuerySort<BannedUsersSort>,
    val offset: Int?,
    val limit: Int?,
    val createdAtAfter: Date?,
    val createdAtAfterOrEqual: Date?,
    val createdAtBefore: Date?,
    val createdAtBeforeOrEqual: Date?,
)
