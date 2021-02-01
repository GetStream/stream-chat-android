package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UserDto(
    val id: String,
    val role: String,
    val invisible: Boolean,
    val banned: Boolean,
)
