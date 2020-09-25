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

sealed class ChatEvent {
    abstract val type: String
    abstract val createdAt: Date
}

sealed class CidEvent : ChatEvent() {
    abstract val cid: String
}

interface UserEvent {
    val user: User
}

data class ChannelCreatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val message: Message?,
    val channel: Channel
) : CidEvent(), UserEvent

data class ChannelDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val ChannelId: String,
    val channel: Channel,
    val user: User?
) : CidEvent()

data class ChannelHiddenEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val user: User?,
    @SerializedName("clear_history") val clearHistory: Boolean
) : CidEvent()

data class ChannelMuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mute") val channelMute: ChannelMute
) : ChatEvent()

data class ChannelsMuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mutes") val channelsMute: List<ChannelMute>
) : ChatEvent()

data class ChannelTruncatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val channel: Channel
) : CidEvent(), UserEvent

data class ChannelUnmuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mute") val channelMute: ChannelMute
) : ChatEvent()

data class ChannelsUnmuteEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("mutes") val channelsMute: List<ChannelMute>
) : ChatEvent()

data class ChannelUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message?,
    val channel: Channel
) : CidEvent()

data class ChannelUpdatedByUserEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val message: Message?,
    val channel: Channel
) : CidEvent(), UserEvent

data class ChannelVisibleEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User
) : CidEvent(), UserEvent

data class HealthEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    @SerializedName("connection_id") val connectionId: String
) : ChatEvent()

data class MemberAddedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val member: Member
) : CidEvent(), UserEvent

data class MemberRemovedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

data class MemberUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val member: Member
) : CidEvent(), UserEvent

data class MessageDeletedEvent(
    override val type: String,
    @SerializedName("deleted_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    @SerializedName("watcher_count") val watcherCount: Int?
) : CidEvent(), UserEvent

data class MessageReadEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("watcher_count") val watcherCount: Int?
) : CidEvent(), UserEvent

data class MessageUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    @SerializedName("watcher_count") val watcherCount: Int?
) : CidEvent(), UserEvent

data class NewMessageEvent(
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

data class NotificationAddedToChannelEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val channel: Channel
) : CidEvent()

data class NotificationChannelDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val ChannelId: String,
    val channel: Channel,
    val user: User?
) : CidEvent()

data class NotificationChannelMutesUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val me: User
) : ChatEvent()

data class NotificationChannelTruncatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val channel: Channel
) : CidEvent(), UserEvent

data class NotificationInviteAcceptedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val member: Member
) : CidEvent(), UserEvent

data class NotificationInvitedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val member: Member
) : CidEvent(), UserEvent

data class NotificationMarkReadEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    @SerializedName("watcher_count") val watcherCount: Int?,
    @SerializedName("total_unread_count") val totalUnreadCount: Int?,
    @SerializedName("unread_channels") val unreadChannels: Int?
) : CidEvent(), UserEvent

data class NotificationMessageNewEvent(
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

data class NotificationMutesUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val me: User
) : ChatEvent()

data class NotificationRemovedFromChannelEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

data class ReactionDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    val reaction: Reaction
) : CidEvent(), UserEvent

data class ReactionNewEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    val reaction: Reaction
) : CidEvent(), UserEvent

data class ReactionUpdateEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    val message: Message,
    val reaction: Reaction
) : CidEvent(), UserEvent

data class TypingStartEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

data class TypingStopEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

data class ChannelUserBannedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User,
    val expiration: Date?
) : CidEvent(), UserEvent

data class GlobalUserBannedEvent(
    override val type: String,
    override val user: User,
    @SerializedName("created_at") override val createdAt: Date
) : ChatEvent(), UserEvent

data class UserDeletedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

data class UserMutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_user") val targetUser: User
) : ChatEvent(), UserEvent

data class UsersMutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_users") val targetUsers: List<User>
) : ChatEvent(), UserEvent

data class UserPresenceChangedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

data class UserStartWatchingEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("watcher_count") val watcherCount: Int,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User
) : CidEvent(), UserEvent

data class UserStopWatchingEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val cid: String,
    @SerializedName("watcher_count") val watcherCount: Int,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String,
    override val user: User
) : CidEvent(), UserEvent

data class ChannelUserUnbannedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    override val cid: String,
    @SerializedName("channel_type") val channelType: String,
    @SerializedName("channel_id") val channelId: String
) : CidEvent(), UserEvent

data class GlobalUserUnbannedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

data class UserUnmutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_user") val targetUser: User
) : ChatEvent(), UserEvent

data class UsersUnmutedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User,
    @SerializedName("target_users") val targetUsers: List<User>
) : ChatEvent(), UserEvent

data class UserUpdatedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    override val user: User
) : ChatEvent(), UserEvent

data class ConnectedEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val me: User,
    @SerializedName("connection_id") val connectionId: String
) : ChatEvent()

data class ConnectingEvent(
    override val type: String,
    override val createdAt: Date
) : ChatEvent()

data class DisconnectedEvent(
    override val type: String,
    override val createdAt: Date
) : ChatEvent()

data class ErrorEvent(
    override val type: String,
    override val createdAt: Date,
    val error: ChatError
) : ChatEvent()

data class UnknownEvent(
    override val type: String,
    @SerializedName("created_at") override val createdAt: Date,
    val rawData: Map<*, *>
) : ChatEvent()
