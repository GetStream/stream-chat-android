package io.getstream.chat.android.client.events

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import java.util.Date

public sealed class ChatEvent {
    public abstract val type: String
    public abstract val createdAt: Date
}

public sealed class CidEvent : ChatEvent() {
    public abstract val cid: String?
}

public interface UserEvent {
    public val user: User
}

public data class ChannelCreatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val message: Message?,
    val channel: Channel
) : CidEvent(), UserEvent

public data class ChannelDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val ChannelId: String,
    val channel: Channel,
    val user: User?
) : CidEvent()

public data class ChannelHiddenEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val user: User?,
    @SerializedName("clear_history") val clearHistory: Boolean
) : CidEvent()

public data class ChannelMuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mute") val channelMute: ChannelMute
) : ChatEvent()

public data class ChannelsMuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mutes") val channelsMute: List<ChannelMute>
) : ChatEvent()

public data class ChannelTruncatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val channel: Channel
) : CidEvent(), UserEvent

public data class ChannelUnmuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mute") val channelMute: ChannelMute
) : ChatEvent()

public data class ChannelsUnmuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mutes") val channelsMute: List<ChannelMute>
) : ChatEvent()

public data class ChannelUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message?,
    val channel: Channel
) : CidEvent()

public data class ChannelUpdatedByUserEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val message: Message?,
    val channel: Channel
) : CidEvent(), UserEvent

public data class ChannelVisibleEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User
) : CidEvent(), UserEvent

public data class HealthEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("connection_id") val connectionId: String
) : ChatEvent()

public data class MemberAddedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val member: Member
) : CidEvent(), UserEvent

public data class MemberRemovedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

public data class MemberUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val member: Member
) : CidEvent(), UserEvent

public data class MessageDeletedEvent(
    override val type: String,
    @SerializedName("deleted_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    @SerializedName("watcher_count") val watcherCount: Int?
) : CidEvent(), UserEvent

public data class MessageReadEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String?,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("watcher_count") val watcherCount: Int?
) : CidEvent(), UserEvent

public data class MessageUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    @SerializedName("watcher_count") val watcherCount: Int?
) : CidEvent(), UserEvent

public data class NewMessageEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    @SerializedName("watcher_count") val watcherCount: Int?,
    @SerializedName("total_unread_count") val totalUnreadCount: Int?,
    @SerializedName("unread_channels") val unreadChannels: Int?
) : CidEvent(), UserEvent

public data class NotificationAddedToChannelEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val channel: Channel
) : CidEvent()

public data class NotificationChannelDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val ChannelId: String,
    val channel: Channel,
    val user: User?
) : CidEvent()

public data class NotificationChannelMutesUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val me: User
) : ChatEvent()

public data class NotificationChannelTruncatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val channel: Channel
) : CidEvent(), UserEvent

public data class NotificationInviteAcceptedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val member: Member
) : CidEvent(), UserEvent

public data class NotificationInvitedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val member: Member
) : CidEvent(), UserEvent

public data class NotificationMarkReadEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String?,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("watcher_count") val watcherCount: Int?,
    @SerializedName("total_unread_count") val totalUnreadCount: Int?,
    @SerializedName("unread_channels") val unreadChannels: Int?
) : CidEvent(), UserEvent

public data class NotificationMessageNewEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    @SerializedName("watcher_count") val watcherCount: Int?,
    @SerializedName("total_unread_count") val totalUnreadCount: Int?,
    @SerializedName("unread_channels") val unreadChannels: Int?
) : CidEvent(), UserEvent

public data class NotificationMutesUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val me: User
) : ChatEvent()

public data class NotificationRemovedFromChannelEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

public data class ReactionDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    val reaction: Reaction
) : CidEvent(), UserEvent

public data class ReactionNewEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    val reaction: Reaction
) : CidEvent(), UserEvent

public data class ReactionUpdateEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    val reaction: Reaction
) : CidEvent(), UserEvent

public data class TypingStartEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

public data class TypingStopEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

public data class ChannelUserBannedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val expiration: Date?
) : CidEvent(), UserEvent

public data class GlobalUserBannedEvent(
    override val type: String,
    override val user: User,
    @SerializedName("created_at") override val createdAt: Date
) : ChatEvent(), UserEvent

public data class UserDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

public data class UserMutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_user") val targetUser: User
) : ChatEvent(), UserEvent

public data class UsersMutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_users") val targetUsers: List<User>
) : ChatEvent(), UserEvent

public data class UserPresenceChangedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

public data class UserStartWatchingEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("watcher_count") val watcherCount: Int,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User
) : CidEvent(), UserEvent

public data class UserStopWatchingEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("watcher_count") val watcherCount: Int,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User
) : CidEvent(), UserEvent

public data class ChannelUserUnbannedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

public data class GlobalUserUnbannedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

public data class UserUnmutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_user") val targetUser: User
) : ChatEvent(), UserEvent

public data class UsersUnmutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_users") val targetUsers: List<User>
) : ChatEvent(), UserEvent

public data class UserUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

public data class ConnectedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val me: User,
    @SerializedName("connection_id") val connectionId: String
) : ChatEvent()

public data class ConnectingEvent(
    override val type: String,
    override val createdAt: Date
) : ChatEvent()

public data class DisconnectedEvent(
    override val type: String,
    override val createdAt: Date
) : ChatEvent()

public data class ErrorEvent(
    override val type: String,
    override val createdAt: Date,
    val error: ChatError
) : ChatEvent()

public data class UnknownEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val rawData: Map<*, *>
) : ChatEvent()
