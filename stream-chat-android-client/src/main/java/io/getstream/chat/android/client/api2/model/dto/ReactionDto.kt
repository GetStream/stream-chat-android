package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal class ReactionDto(
    val message_id: String,
    val type: String,
    val score: Int,
    val user: UserDto?,
    val user_id: String,
    val created_at: Date?,
    val updated_at: Date?,
)
