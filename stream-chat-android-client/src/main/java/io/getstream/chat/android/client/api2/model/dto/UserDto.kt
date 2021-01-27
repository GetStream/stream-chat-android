package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UserDto(
    var id: String,
    var role: String,
    var invisible: Boolean,
    var banned: Boolean,
)
