package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.models.ChannelInfo
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class DownstreamMessageDto(
    val id: String,
    val cid: String,
    val parent_id: String?,

    val type: String,
    val text: String,
    val html: String,
    val user: UserDto,

    val reply_count: Int,
    val reaction_counts: Map<String, Int>,
    val reaction_scores: Map<String, Int>,

    val latest_reactions: List<ReactionDto>,
    val own_reactions: List<ReactionDto>,

    val command: String?,
    val silent: Boolean,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val mentioned_users: List<UserDto>,
    val i18n: Map<String, String>,
    val thread_participants: List<UserDto>,

    val attachments: List<AttachmentDto>,

    val created_at: Date,
    val updated_at: Date,
    val deleted_at: Date?,

    val quoted_message: DownstreamMessageDto?,
    val quoted_message_id: String?,

    val pinned: Boolean,
    val pinned_at: Date?,
    val pin_expires: Date?,
    val pinned_by: UserDto?,

    val channel: ChannelInfo,

    val extraData: Map<String, Any>,
)
