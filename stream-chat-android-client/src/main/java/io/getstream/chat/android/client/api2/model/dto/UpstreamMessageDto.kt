package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class UpstreamMessageDto(
    val id: String,
    val cid: String,
    val parent_id: String?,

    val text: String,
    val html: String,
    val user: UserDto,

    val command: String?,
    val silent: Boolean,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val mentioned_users: List<String>,

    val attachments: List<AttachmentDto>,

    val quoted_message: UpstreamMessageDto?,
    val quoted_message_id: String?,

    val pinned: Boolean,
    val pinned_at: Date?,
    val pin_expires: Date?,
    val pinned_by: UserDto?,

    val thread_participants: List<UserDto>,

    val extraData: Map<String, Any>,
)
