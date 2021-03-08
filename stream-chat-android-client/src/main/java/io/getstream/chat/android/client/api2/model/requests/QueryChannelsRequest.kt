package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal class QueryChannelsRequest(
    val filter_conditions: Map<*, *>,
    val offset: Int,
    val limit: Int,
    val querySort: List<Map<String, Any>>,
    val message_limit: Int = 0,
    val member_limit: Int = 0,
    val state: Boolean,
    val watch: Boolean,
    val presence: Boolean,
)
