package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import java.util.Date

internal data class QueryBannedUsersRequest(
    @SerializedName("filter_conditions")
    var filter: FilterObject,
    val sort: List<Map<String, Any>>,
    val offset: Int?,
    val limit: Int?,
    @SerializedName("created_at_after")
    val createdAtAfter: Date?,
    @SerializedName("created_at_after_or_equal")
    val createdAtAfterOrEqual: Date?,
    @SerializedName("created_at_before")
    val createdAtBefore: Date?,
    @SerializedName("created_at_before_or_equal")
    val createdAtBeforeOrEqual: Date?,
)
