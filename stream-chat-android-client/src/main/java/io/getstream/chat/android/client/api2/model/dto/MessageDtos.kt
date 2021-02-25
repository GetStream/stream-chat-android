package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

/**
 * See [io.getstream.chat.android.client.parser2.adapters.UpstreamMessageDtoAdapter] for
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
    val pinned_by: UpstreamUserDto?,
    val quoted_message: UpstreamMessageDto?,
    val quoted_message_id: String?,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<UpstreamUserDto>,
    val user: UpstreamUserDto,

    val extraData: Map<String, Any>,
)

/**
 * See [io.getstream.chat.android.client.parser2.adapters.DownstreamMessageDtoAdapter] for
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
    val latest_reactions: List<DownstreamReactionDto>,
    val mentioned_users: List<DownstreamUserDto>,
    val own_reactions: List<DownstreamReactionDto>,
    val parent_id: String?,
    val pin_expires: Date?,
    val pinned: Boolean,
    val pinned_at: Date?,
    val pinned_by: DownstreamUserDto?,
    val quoted_message: DownstreamMessageDto?,
    val quoted_message_id: String?,
    val reaction_counts: Map<String, Int>,
    val reaction_scores: Map<String, Int>,
    val reply_count: Int,
    val shadowed: Boolean,
    val show_in_channel: Boolean,
    val silent: Boolean,
    val text: String,
    val thread_participants: List<DownstreamUserDto>,
    val type: String,
    val updated_at: Date,
    val user: DownstreamUserDto,

    val extraData: Map<String, Any>,
)
