package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class BanUserRequest(
    var target_user_id: String,
    var timeout: Int?,
    var reason: String?,
    var type: String,
    var id: String,
    val shadow: Boolean,
)
