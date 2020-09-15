package io.getstream.chat.android.client.api.models

import io.getstream.chat.android.client.parser.IgnoreSerialisation
import io.getstream.chat.android.client.utils.FilterObject

data class SearchMessagesRequest(
    val offset: Int,
    val limit: Int,
    @IgnoreSerialisation
    val channelFilter: FilterObject,
    @IgnoreSerialisation
    val messageFilter: FilterObject
) {
    val filter_conditions = channelFilter.toMap()
    val message_filter_conditions = messageFilter.toMap()
}
