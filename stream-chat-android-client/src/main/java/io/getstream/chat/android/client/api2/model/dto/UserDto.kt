package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

/**
 * See [io.getstream.chat.android.client.parser2.UserDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class UserDto(
    val banned: Boolean,
    val id: String,
    val invisible: Boolean,
    val role: String,

    val extraData: Map<String, Any>,
)
