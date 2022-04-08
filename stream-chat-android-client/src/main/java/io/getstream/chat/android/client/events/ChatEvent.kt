/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.events

import io.getstream.chat.android.client.clientstate.DisconnectCause
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
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
    public abstract val cid: String
    public abstract val channelType: String
    public abstract val channelId: String
}

public sealed interface UserEvent {
    public val user: User
}

public sealed interface HasChannel {
    public val channel: Channel
}

public sealed interface HasMessage {
    public val message: Message
}

public sealed interface HasReaction {
    public val reaction: Reaction
}

public sealed interface HasMember {
    public val member: Member
}

public sealed interface HasOwnUser {
    public val me: User
}

/**
 * Interface that marks a [ChatEvent] as having the information about watcher count.
 *
 * The list of events which contain watcher count:
 * - user.watching_start
 * - user.watching_stop
 * - message.new
 */
public sealed interface HasWatcherCount {
    public val watcherCount: Int
}

/**
 * Interface that marks a [ChatEvent] as having the information about unread counts. There are certain
 * cases when the server omits these fields (e.g. when `ReadEvents` option is disabled, when the number of
 * watchers is over 100, etc). In that case `totalUnreadCount` and `unreadChannels` fields have 0 values.
 *
 * The list of events which contain unread counts:
 * - message.new
 * - notification.message_new
 * - notification.mark_read
 * - notification.added_to_channel
 * - notification.channel_deleted
 * - notification.channel_truncated
 */
public sealed interface HasUnreadCounts {
    public val totalUnreadCount: Int
    public val unreadChannels: Int
}

/**
 * Triggered when a channel is deleted
 */
public data class ChannelDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    val user: User?,
) : CidEvent(), HasChannel

/**
 * Triggered when a channel is mark as hidden
 */
public data class ChannelHiddenEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val clearHistory: Boolean,
) : CidEvent(), UserEvent

/**
 * Triggered when a channels' history is truncated. Could contain system [message].
 */
public data class ChannelTruncatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val user: User?,
    val message: Message?,
    override val channel: Channel,
) : CidEvent(), HasChannel

/**
 * Triggered when a channel is updated. Could contain system [message].
 */
public data class ChannelUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val message: Message?,
    override val channel: Channel,
) : CidEvent(), HasChannel

/**
 * Triggered when a channel is updated by user. Could contain system [message].
 */
public data class ChannelUpdatedByUserEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val message: Message?,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasChannel

/**
 * Triggered when a channel is made visible
 */
public data class ChannelVisibleEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
) : CidEvent(), UserEvent

/**
 * Triggered every 30 second to confirm that the client connection is still alive
 */
public data class HealthEvent(
    override val type: String,
    override val createdAt: Date,
    val connectionId: String,
) : ChatEvent()

/**
 * Triggered when a member is added to a channel
 */
public data class MemberAddedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

/**
 * Triggered when a member is removed from a channel
 */
public data class MemberRemovedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

/**
 * Triggered when a channel member is updated (promoted to moderator/accepted/.rejected the invite)
 */
public data class MemberUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

/**
 * Triggered when a message is deleted
 */
public data class MessageDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    val user: User?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    val hardDelete: Boolean,
) : CidEvent(), HasMessage

/**
 * Triggered when a channel is marked as read
 */
public data class MessageReadEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
) : CidEvent(), UserEvent

/**
 * Triggered when a message is updated
 */
public data class MessageUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
) : CidEvent(), UserEvent, HasMessage

/**
 * Triggered when a new message is added on a channel.
 */
public data class NewMessageEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val watcherCount: Int = 0,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), UserEvent, HasMessage, HasWatcherCount, HasUnreadCounts

/**
 * Triggered when the user is added to the list of channel members
 */
public data class NotificationAddedToChannelEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasUnreadCounts

/**
 * Triggered when a channel is deleted
 */
public data class NotificationChannelDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasUnreadCounts

/**
 * Triggered when a channel is muted
 */
public data class NotificationChannelMutesUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val me: User,
) : ChatEvent(), HasOwnUser

/**
 * Triggered when a channels' history is truncated
 */
public data class NotificationChannelTruncatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasUnreadCounts

/**
 * Triggered when the user accepts an invite
 */
public data class NotificationInviteAcceptedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val member: Member,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasMember, HasChannel

/**
 * Triggered when the user rejects an invite
 */
public data class NotificationInviteRejectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val member: Member,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasMember, HasChannel

/**
 * Triggered when the user is invited to join a channel
 */
public data class NotificationInvitedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val member: Member,
) : CidEvent(), UserEvent, HasMember

/**
 * Triggered when the count of unread messages for a particular channel changes
 */
public data class NotificationMarkReadEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), UserEvent, HasUnreadCounts

/**
 * Triggered when the total count of unread messages (across all channels the user is a member) changes
 */
public data class MarkAllReadEvent(
    override val type: String = "",
    override val createdAt: Date,
    override val user: User,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : ChatEvent(), UserEvent, HasUnreadCounts

/**
 * Triggered when a message is added to a channel
 */
public data class NotificationMessageNewEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val message: Message,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasMessage, HasUnreadCounts

/**
 * Triggered when the user mutes are updated
 */
public data class NotificationMutesUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val me: User,
) : ChatEvent(), HasOwnUser

/**
 * Triggered when a user is removed from the list of channel members
 */
public data class NotificationRemovedFromChannelEvent(
    override val type: String,
    override val createdAt: Date,
    val user: User?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val member: Member,
) : CidEvent(), HasMember, HasChannel

/**
 * Triggered when a message reaction is deleted
 */
public data class ReactionDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val reaction: Reaction,
) : CidEvent(), UserEvent, HasMessage, HasReaction

/**
 * Triggered when a message reaction is added
 */
public data class ReactionNewEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val reaction: Reaction,
) : CidEvent(), UserEvent, HasMessage, HasReaction

/**
 * Triggered when a message reaction is updated
 */
public data class ReactionUpdateEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val reaction: Reaction,
) : CidEvent(), UserEvent, HasMessage, HasReaction

/**
 * Triggered when a user starts typing
 */
public data class TypingStartEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val parentId: String?,
) : CidEvent(), UserEvent

/**
 * Triggered when a user stops typing
 */
public data class TypingStopEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val parentId: String?,
) : CidEvent(), UserEvent

/**
 * Triggered when the user is banned from a channel
 */
public data class ChannelUserBannedEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val expiration: Date?,
) : CidEvent(), UserEvent

/**
 * Triggered when the user is banned globally
 */
public data class GlobalUserBannedEvent(
    override val type: String,
    override val user: User,
    override val createdAt: Date,
) : ChatEvent(), UserEvent

public data class UserDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user status changes (eg. online, offline, away, etc.)
 */
public data class UserPresenceChangedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user starts watching a channel
 */
public data class UserStartWatchingEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val watcherCount: Int = 0,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
) : CidEvent(), UserEvent, HasWatcherCount

/**
 * Triggered when a user stops watching a channel
 */
public data class UserStopWatchingEvent(
    override val type: String,
    override val createdAt: Date,
    override val cid: String,
    override val watcherCount: Int = 0,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
) : CidEvent(), UserEvent, HasWatcherCount

/**
 * Triggered when the channel user ban is lifted
 */
public data class ChannelUserUnbannedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
) : CidEvent(), UserEvent

/**
 * Triggered when the global user ban is lifted
 */
public data class GlobalUserUnbannedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user is updated
 */
public data class UserUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user gets connected to the WS
 */
public data class ConnectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val me: User,
    val connectionId: String,
) : ChatEvent(), HasOwnUser

/**
 * Triggered when a user is connecting to the WS
 */
public data class ConnectingEvent(
    override val type: String,
    override val createdAt: Date,
) : ChatEvent()

/**
 * Triggered when a user gets disconnected to the WS
 */
public data class DisconnectedEvent(
    override val type: String,
    override val createdAt: Date,
    val disconnectCause: DisconnectCause = DisconnectCause.NetworkNotAvailable,
) : ChatEvent()

/**
 * Triggered when WS connection emits error
 */
public data class ErrorEvent(
    override val type: String,
    override val createdAt: Date,
    val error: ChatError,
) : ChatEvent()

/**
 * Triggered when event type is not supported
 */
public data class UnknownEvent(
    override val type: String,
    override val createdAt: Date,
    val user: User?,
    val rawData: Map<*, *>,
) : ChatEvent()
