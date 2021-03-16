package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class QueryBannedUsersRequest(
    var filter_conditions: Map<*, *>,
    val sort: List<Map<String, Any>>,
    val offset: Int?,
    val limit: Int?,
    val created_at_after: Date?,
    val created_at_after_or_equal: Date?,
    val created_at_before: Date?,
    val created_at_before_or_equal: Date?,
)
