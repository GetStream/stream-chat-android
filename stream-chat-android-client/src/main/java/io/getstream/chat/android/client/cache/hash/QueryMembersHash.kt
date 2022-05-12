package io.getstream.chat.android.client.cache.hash

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Member

internal data class QueryMembersHash(
    val channelType: String,
    val channelId: String,
    val offset: Int,
    val limit: Int,
    val filter: FilterObject,
    val sort: QuerySort<Member>,
    val members: List<Member>,
)
