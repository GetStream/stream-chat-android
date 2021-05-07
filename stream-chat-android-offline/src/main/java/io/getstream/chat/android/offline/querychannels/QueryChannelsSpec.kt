package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import java.util.Objects

internal data class QueryChannelsSpec(
    val filter: FilterObject,
    val sort: QuerySort<Channel>,
    var cids: List<String> = emptyList()
) {
    val id: String
        get() = filter.hashCode().toString() + Objects.hash(sort.toDto()).toString()
}
