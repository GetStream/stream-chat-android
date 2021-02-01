package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * See [io.getstream.chat.android.client.parser2.UpstreamMessageDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamMessageDto(
    val attachments: List<AttachmentDto>,
    val cid: String,
    val command: String?,
    val html: String,
    val id: String,
    val mentioned_users: List<String>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean,
    val pinned_at: Date?,
    val pinned_by: UserDto?,
    val quoted_message: UpstreamMessageDto?,
    val quoted_message_id: String?,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<UserDto>,
    val user: UserDto,

    val extraData: Map<String, Any>,
)
