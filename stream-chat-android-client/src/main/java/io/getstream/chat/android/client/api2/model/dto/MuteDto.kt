package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class MuteDto(
    val user: UserDto,
    val target: UserDto,
    val created_at: Date,
    val updated_at: Date,
)
