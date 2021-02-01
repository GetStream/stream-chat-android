package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * See [io.getstream.chat.android.client.parser2.DownstreamMessageDtoAdapter] for
 * special [extraData] handling.
 */
@JsonClass(generateAdapter = true)
internal data class DownstreamMessageDto(
    val attachments: List<AttachmentDto>,
    val channel: ChannelInfoDto?,
    val cid: String,
    val command: String?,
    val created_at: Date,
    val deleted_at: Date?,
    val html: String,
    val i18n: Map<String, String>,
    val id: String,
    val latest_reactions: List<ReactionDto>,
    val mentioned_users: List<UserDto>,
    val own_reactions: List<ReactionDto>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean,
    val pinned_at: Date?,
    val pinned_by: UserDto?,
    val quoted_message: DownstreamMessageDto?,
    val quoted_message_id: String?,
    val reaction_counts: Map<String, Int>,
    val reaction_scores: Map<String, Int>,
    val reply_count: Int,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<UserDto>,
    val type: String,
    val updated_at: Date,
    val user: UserDto,

    val extraData: Map<String, Any>,
)
