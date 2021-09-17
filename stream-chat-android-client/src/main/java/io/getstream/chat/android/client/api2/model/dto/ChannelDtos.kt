package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class UpstreamChannelDto(
    val cid: String,
    val id: String,
    val type: String,
    val watcherCount: Int,
    val frozen: Boolean,
    val last_message_at: Date?,
    val created_at: Date?,
    val deleted_at: Date?,
    val updated_at: Date?,
    val member_count: Int,
    val messages: List<UpstreamMessageDto>,
    val members: List<UpstreamMemberDto>,
    val watchers: List<UpstreamUserDto>,
    val read: List<UpstreamChannelUserRead>,
    val config: ConfigDto,
    val created_by: UpstreamUserDto,
    val team: String,
    val cooldown: Int,
    val pinned_messages: List<UpstreamMessageDto>,

    val extraData: Map<String, Any>,
) : ExtraDataDto

@JsonClass(generateAdapter = true)
internal data class DownstreamChannelDto(
    val cid: String,
    val id: String,
    val type: String,
    val watcher_count: Int = 0,
    val frozen: Boolean,
    val last_message_at: Date?,
    val created_at: Date?,
    val deleted_at: Date?,
    val updated_at: Date?,
    val member_count: Int = 0,
    val messages: List<DownstreamMessageDto> = emptyList(),
    val members: List<DownstreamMemberDto> = emptyList(),
    val watchers: List<DownstreamUserDto> = emptyList(),
    val read: List<DownstreamChannelUserRead> = emptyList(),
    val config: ConfigDto,
    val created_by: DownstreamUserDto?,
    val team: String = "",
    val cooldown: Int = 0,
    val pinned_messages: List<DownstreamMessageDto> = emptyList(),

    val extraData: Map<String, Any>,
) : ExtraDataDto
