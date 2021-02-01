package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class DownstreamMessageDto(
    val id: String,
    val cid: String,
    val text: String,
    val html: String,
    val parent_id: String?,
    val command: String?,
    val user: UserDto,
    val silent: Boolean,
    val shadowed: Boolean,
    val created_at: Date,
    val updated_at: Date,
    val deleted_at: Date?,
    val extraData: Map<String, Any>,
)
