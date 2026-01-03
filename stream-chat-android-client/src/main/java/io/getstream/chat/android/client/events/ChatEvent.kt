/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.DraftMessage
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.result.Error
import java.util.Date
import java.util.concurrent.atomic.AtomicInteger

private val seqGenerator = AtomicInteger()

public sealed class ChatEvent {
    public abstract val type: String
    public abstract val createdAt: Date
    public abstract val rawCreatedAt: String?

    public val seq: Int = seqGenerator.incrementAndGet()
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

public sealed interface HasPoll {
    public val messageId: String?
    public val poll: Poll
}

public sealed interface HasReminder {
    public val reminder: MessageReminder
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
 * - notification.mark_unread
 * - notification.added_to_channel
 * - notification.channel_deleted
 * - notification.channel_truncated
 */
public sealed interface HasUnreadCounts {
    public val totalUnreadCount: Int
    public val unreadChannels: Int
}

/**
 * Interface that marks a [ChatEvent] as having the information about unread thread counts.
 *
 * The list of events which contains unread counts:
 * - notification.thread_message_new
 */
public sealed interface HasUnreadThreadCounts {
    public val unreadThreads: Int?
    public val unreadThreadMessages: Int?
}

/**
 * Triggered when a channel is deleted
 */
public data class ChannelDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val channel: Channel,
    val clearHistory: Boolean,
) : CidEvent(), UserEvent, HasChannel

/**
 * Triggered when a channels' history is truncated. Could contain system [message].
 */
public data class ChannelTruncatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    val user: User?,
    val message: Message?,
) : CidEvent(), HasChannel

/**
 * Triggered when a channel is updated. Could contain system [message].
 */
public data class ChannelUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    val message: Message?,
) : CidEvent(), HasChannel

/**
 * Triggered when a channel is updated by user. Could contain system [message].
 */
public data class ChannelUpdatedByUserEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val channel: Channel,
    val message: Message?,
) : CidEvent(), UserEvent, HasChannel

/**
 * Triggered when a channel is made visible
 */
public data class ChannelVisibleEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    override val channel: Channel,
) : CidEvent(), UserEvent, HasChannel

/**
 * Triggered every 30 second to confirm that the client connection is still alive
 */
public data class HealthEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val connectionId: String,
) : ChatEvent()

/**
 * Triggered when a member is added to a channel
 */
public data class MemberAddedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    val user: User?,
    val hardDelete: Boolean,
    val channelMessageCount: Int?,
    val deletedForMe: Boolean,
) : CidEvent(), HasMessage

/**
 * Triggered when a channel is marked as read
 */
public data class MessageReadEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val lastReadMessageId: String?,
    val thread: ThreadInfo? = null,
) : CidEvent(), UserEvent

/**
 * Triggered when a message is marked as delivered
 */
public data class MessageDeliveredEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val lastDeliveredAt: Date,
    val lastDeliveredMessageId: String,
) : CidEvent(), UserEvent

/**
 * Triggered when a message is updated
 */
public data class MessageUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
) : CidEvent(), UserEvent, HasMessage

public data class DraftMessageUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val draftMessage: DraftMessage,
) : ChatEvent()

public data class DraftMessageDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val draftMessage: DraftMessage,
) : ChatEvent()

/**
 * Triggered when a new message is added on a channel.
 */
public data class NewMessageEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val message: Message,
    override val watcherCount: Int = 0,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
    val channelMessageCount: Int?,
) : CidEvent(), UserEvent, HasMessage, HasWatcherCount, HasUnreadCounts

/**
 * Triggered when the user is added to the list of channel members
 */
public data class NotificationAddedToChannelEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val member: Member,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasMember, HasUnreadCounts

/**
 * Triggered when a channel is deleted
 */
public data class NotificationChannelDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val me: User,
) : ChatEvent(), HasOwnUser

/**
 * Triggered when a channels' history is truncated
 */
public data class NotificationChannelTruncatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
    val lastReadMessageId: String?,
    val threadId: String? = null,
    val thread: ThreadInfo? = null,
    override val unreadThreads: Int? = null,
    override val unreadThreadMessages: Int? = null,
) : CidEvent(), UserEvent, HasUnreadCounts, HasUnreadThreadCounts

/**
 * Triggered when the the user mark as unread a conversation from a particular message
 */
public data class NotificationMarkUnreadEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
    val unreadMessages: Int,
    val firstUnreadMessageId: String,
    val lastReadMessageAt: Date,
    val lastReadMessageId: String?,
    val threadId: String? = null,
    override val unreadThreads: Int = 0,
    override val unreadThreadMessages: Int? = null,
) : CidEvent(), UserEvent, HasUnreadCounts, HasUnreadThreadCounts

/**
 * Triggered when the total count of unread messages (across all channels the user is a member) changes
 */
public data class MarkAllReadEvent(
    override val type: String = "",
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val message: Message,
    override val totalUnreadCount: Int = 0,
    override val unreadChannels: Int = 0,
) : CidEvent(), HasChannel, HasMessage, HasUnreadCounts

/**
 * Triggered when a message is added to a channel as a thread reply.
 */
public data class NotificationThreadMessageNewEvent(
    override val type: String,
    override val cid: String,
    override val channelId: String,
    override val channelType: String,
    override val message: Message,
    override val channel: Channel,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val unreadThreads: Int,
    override val unreadThreadMessages: Int,
) : CidEvent(), HasMessage, HasChannel, HasUnreadThreadCounts

/**
 * Triggered when the user mutes are updated
 */
public data class NotificationMutesUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val me: User,
) : ChatEvent(), HasOwnUser

/**
 * Triggered when a user is removed from the list of channel members
 */
public data class NotificationRemovedFromChannelEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val channel: Channel,
    override val member: Member,
    val user: User?,
) : CidEvent(), HasMember, HasChannel

/**
 * Triggered when a message reaction is deleted
 */
public data class ReactionDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val user: User,
    val expiration: Date?,
    val shadow: Boolean,
) : CidEvent(), UserEvent

/**
 * Triggered when the user is banned globally
 */
public data class GlobalUserBannedEvent(
    override val type: String,
    override val user: User,
    override val createdAt: Date,
    override val rawCreatedAt: String,
) : ChatEvent(), UserEvent

public data class UserDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user status changes (eg. online, offline, away, etc.)
 */
public data class UserPresenceChangedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user starts watching a channel
 */
public data class UserStartWatchingEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
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
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a user is updated
 */
public data class UserUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val user: User,
) : ChatEvent(), UserEvent

/**
 * Triggered when a poll is updated.
 */
public data class PollUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
) : CidEvent(), HasPoll

/**
 * Triggered when a poll is deleted.
 */
public data class PollDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
) : CidEvent(), HasPoll

/**
 * Triggered when a poll is closed.
 */
public data class PollClosedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
) : CidEvent(), HasPoll

/**
 * Triggered when a vote is casted.
 */
public data class VoteCastedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
    val newVote: Vote,
) : CidEvent(), HasPoll

/**
 * Triggered when a vote is casted.
 */
public data class AnswerCastedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
    val newAnswer: Answer,
) : CidEvent(), HasPoll

/**
 * Triggered when a vote is changed.
 */
public data class VoteChangedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
    val newVote: Vote,
) : CidEvent(), HasPoll

/**
 * Triggered when a vote is removed.
 */
public data class VoteRemovedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val messageId: String?,
    override val poll: Poll,
    val removedVote: Vote,
) : CidEvent(), HasPoll

/**
 * Triggered when a message reminder is created.
 */
public data class ReminderCreatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val reminder: MessageReminder,
    val messageId: String,
    val userId: String,
) : CidEvent(), HasReminder

/**
 * Triggered when a message reminder is updated.
 */
public data class ReminderUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val reminder: MessageReminder,
    val messageId: String,
    val userId: String,
) : CidEvent(), HasReminder

/**
 * Triggered when a message reminder is deleted.
 */
public data class ReminderDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val reminder: MessageReminder,
    val messageId: String,
    val userId: String,
) : CidEvent(), HasReminder

/**
 * Triggered when a message reminder is due.
 */
public data class NotificationReminderDueEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    override val reminder: MessageReminder,
    val messageId: String,
    val userId: String,
) : CidEvent(), HasReminder

/**
 * Event triggered after a user was banned and their messages were deleted. Triggered in two scenarios:
 * 1. User banned in a channel - all messages of the user in that channel were deleted ([cid] != null).
 * 2. User banned globally - all messages of the user across all channels were deleted ([cid] == null).
 */
public data class UserMessagesDeletedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val user: User,
    val cid: String?,
    val channelType: String?,
    val channelId: String?,
    val hardDelete: Boolean,
) : ChatEvent(), UserEvent

/**
 * Triggered when an ai indicator is updated.
 */
public data class AIIndicatorUpdatedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
    val aiState: String,
    val messageId: String,
) : CidEvent(), UserEvent

/**
 * Triggered when an ai indicator is cleared.
 */
public data class AIIndicatorClearEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
) : CidEvent(), UserEvent

/**
 * Triggered when an ai indicator is stopped.
 */
public data class AIIndicatorStopEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    override val user: User,
    override val cid: String,
    override val channelType: String,
    override val channelId: String,
) : CidEvent(), UserEvent

/**
 * Triggered when a user gets connected to the WS
 */
public data class ConnectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    override val me: User,
    val connectionId: String,
) : ChatEvent(), HasOwnUser

/**
 * Triggered when a WS connection fails.
 */
public data class ConnectionErrorEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val connectionId: String,
    val error: ChatError,
) : ChatEvent()

/**
 * Triggered when a user is connecting to the WS
 */
public data class ConnectingEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
) : ChatEvent()

/**
 * Triggered when a user gets disconnected to the WS
 */
public data class DisconnectedEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    val disconnectCause: DisconnectCause = DisconnectCause.NetworkNotAvailable,
) : ChatEvent()

/**
 * Triggered when WS connection emits error
 */
public data class ErrorEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String?,
    val error: Error,
) : ChatEvent()

/**
 * Triggered when event type is not supported
 */
public data class UnknownEvent(
    override val type: String,
    override val createdAt: Date,
    override val rawCreatedAt: String,
    val user: User?,
    val rawData: Map<*, *>,
) : ChatEvent()
