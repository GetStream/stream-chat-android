package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MuteUserRequest(
    val target_id: String,
    val user_id: String,
    val timeout: Int?,
)
