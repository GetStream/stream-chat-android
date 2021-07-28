package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class DownstreamFlagDto(
    val user: DownstreamUserDto,
    val target_user: DownstreamUserDto?,
    val target_message_id: String,
    val created_at: String,
    val created_by_automod: Boolean,
    val approved_at: Date,
    val updated_at: Date,
    val reviewed_at: Date,
    val reviewed_by: Date?,
    val rejected_at: Date,
)
