package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SearchMessagesRequest(
    val offset: Int,
    val limit: Int,
    val filter_conditions: Map<*, *>,
    val message_filter_conditions: Map<*, *>,
)
