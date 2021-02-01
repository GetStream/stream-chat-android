package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class UpstreamMessageDto(
    val id: String,
    val cid: String,
    val text: String,
    val html: String,
    val parent_id: String?,
    val command: String?,
    val user: UserDto,
    val silent: Boolean,
    val shadowed: Boolean,
    val extraData: Map<String, Any>,
)
