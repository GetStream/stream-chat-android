package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

/**
 * See [io.getstream.chat.android.client.parser2.UserDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class UserDto(
    val id: String,
    val role: String,
    val invisible: Boolean,
    val banned: Boolean,
    val extraData: Map<String, Any>,
)
