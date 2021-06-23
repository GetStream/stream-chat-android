package io.getstream.chat.android.client.api2.model.dto

import com.squareup.moshi.JsonClass
import io.getstream.chat.android.client.errors.ChatError
import java.util.Date

internal sealed class ChatEventDto

@JsonClass(generateAdapter = true)
internal data class ChannelDeletedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val user: DownstreamUserDto?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelHiddenEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val clear_history: Boolean,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelTruncatedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUpdatedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto?,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUpdatedByUserEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val message: DownstreamMessageDto?,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelVisibleEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class HealthEventDto(
    val type: String,
    val created_at: Date,
    val connection_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberAddedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberRemovedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MemberUpdatedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageDeletedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageReadEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MessageUpdatedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NewMessageEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val watcher_count: Int = 0,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationAddedToChannelEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelDeletedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelMutesUpdatedEventDto(
    val type: String,
    val created_at: Date,
    val me: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationChannelTruncatedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInviteAcceptedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val member: DownstreamMemberDto,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInviteRejectedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val member: DownstreamMemberDto,
    val channel: DownstreamChannelDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationInvitedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMarkReadEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class MarkAllReadEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMessageNewEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val channel: DownstreamChannelDto,
    val message: DownstreamMessageDto,
    val total_unread_count: Int = 0,
    val unread_channels: Int = 0,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationMutesUpdatedEventDto(
    val type: String,
    val created_at: Date,
    val me: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class NotificationRemovedFromChannelEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto?,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val member: DownstreamMemberDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionDeletedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionNewEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ReactionUpdateEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val message: DownstreamMessageDto,
    val reaction: DownstreamReactionDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class TypingStartEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val parent_id: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class TypingStopEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val parent_id: String?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUserBannedEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
    val expiration: Date?,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class GlobalUserBannedEventDto(
    val type: String,
    val user: DownstreamUserDto,
    val created_at: Date,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserDeletedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserPresenceChangedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserStartWatchingEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val watcher_count: Int = 0,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserStopWatchingEventDto(
    val type: String,
    val created_at: Date,
    val cid: String,
    val watcher_count: Int = 0,
    val channel_type: String,
    val channel_id: String,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ChannelUserUnbannedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
    val cid: String,
    val channel_type: String,
    val channel_id: String,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class GlobalUserUnbannedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UserUpdatedEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ConnectedEventDto(
    val type: String,
    val created_at: Date,
    val me: DownstreamUserDto,
    val connection_id: String,
) : ChatEventDto()

/**
 * Special upstream event class, as we have to send this event
 * after connecting.
 */
@JsonClass(generateAdapter = true)
internal data class UpstreamConnectedEventDto(
    val type: String,
    val created_at: Date,
    val me: UpstreamUserDto,
    val connection_id: String,
)

@JsonClass(generateAdapter = true)
internal data class ConnectingEventDto(
    val type: String,
    val createdAt: Date,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class DisconnectedEventDto(
    val type: String,
    val createdAt: Date,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class ErrorEventDto(
    val type: String,
    val createdAt: Date,
    val error: ChatError,
) : ChatEventDto()

@JsonClass(generateAdapter = true)
internal data class UnknownEventDto(
    val type: String,
    val created_at: Date,
    val user: DownstreamUserDto?,
    val rawData: Map<*, *>,
) : ChatEventDto()
