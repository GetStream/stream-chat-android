package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class QueryUsersRequest(
    val filter_conditions: Map<*, *>,
    val offset: Int,
    val limit: Int,
    val sort: List<Map<String, Any>>,
    val presence: Boolean,
)
