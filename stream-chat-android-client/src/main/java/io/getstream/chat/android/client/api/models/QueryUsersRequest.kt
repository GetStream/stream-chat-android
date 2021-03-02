package io.getstream.chat.android.client.api.models

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.parser.IgnoreSerialisation

public data class QueryUsersRequest @JvmOverloads constructor(
    @SerializedName("filter_conditions")
    var filter: FilterObject,
    val offset: Int,
    val limit: Int,
    @IgnoreSerialisation
    var querySort: QuerySort<User> = QuerySort(),
    var presence: Boolean = false
) {
    val sort: List<Map<String, Any>> = querySort.toDto()
}
