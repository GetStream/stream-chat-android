package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject

internal data class QueryChannelsSpec(
    val filter: FilterObject,
    var cids: List<String> = emptyList()
) {
    val id: String
        get() = filter.hashCode().toString()
}
