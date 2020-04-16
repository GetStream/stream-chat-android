package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject


data class SearchMessagesRequest(
    val query: String,
    val offset: Int,
    val limit: Int,
    @IgnoreSerialisation
    val filter: FilterObject
) {
    val filter_conditions = filter.toMap()
}

