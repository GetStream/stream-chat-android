package io.getstream.chat.android.client.offline.model

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public data class QueryChannelsSpec(
    val filter: FilterObject,
    var cids: List<String> = emptyList()
) {
    val id: String
        get() = filter.hashCode().toString()
}
