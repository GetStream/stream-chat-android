package io.getstream.chat.android.offline.model.querychannels.internal

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel

internal data class QueryChannelsSpec(
    val filter: FilterObject,
    val querySort: QuerySort<Channel>
) {
    var cids: Set<String> = emptySet()
}
