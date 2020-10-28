package io.getstream.chat.android.livedata.controller

import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.FilterObject

internal data class QueryChannelsSpec(
    val filter: FilterObject,
    val sort: QuerySort<Channel>,
    var cids: List<String> = emptyList()
)