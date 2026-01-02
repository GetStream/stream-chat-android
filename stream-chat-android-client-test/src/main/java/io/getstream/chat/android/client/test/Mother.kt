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

package io.getstream.chat.android.client.test

import io.getstream.chat.android.client.events.AnswerCastedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MessageDeliveredEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationReminderDueEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.PollClosedEvent
import io.getstream.chat.android.client.events.PollDeletedEvent
import io.getstream.chat.android.client.events.PollUpdatedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReminderCreatedEvent
import io.getstream.chat.android.client.events.ReminderDeletedEvent
import io.getstream.chat.android.client.events.ReminderUpdatedEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserMessagesDeletedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.VoteCastedEvent
import io.getstream.chat.android.client.events.VoteChangedEvent
import io.getstream.chat.android.client.events.VoteRemovedEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Answer
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.MessageReminder
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.Poll
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.ThreadInfo
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.positiveRandomInt
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageReminder
import io.getstream.chat.android.randomPoll
import io.getstream.chat.android.randomPollAnswer
import io.getstream.chat.android.randomPollVote
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomThreadInfo
import io.getstream.chat.android.randomUser
import java.util.Date

private val streamFormatter = StreamDateFormatter()

public fun randomConnectedEvent(
    createdAt: Date = randomDate(),
    me: User = randomUser(),
    connectionId: String = randomString(),
): ConnectedEvent = ConnectedEvent(
    type = EventType.HEALTH_CHECK,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    me = me,
    connectionId = connectionId,
)

public fun randomNotificationChannelMutesUpdatedEvent(
    createdAt: Date = randomDate(),
    me: User = randomUser(),
): NotificationChannelMutesUpdatedEvent = NotificationChannelMutesUpdatedEvent(
    type = EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    me = me,
)

public fun randomNotificationMutesUpdatedEvent(
    createdAt: Date = randomDate(),
    me: User = randomUser(),
): NotificationMutesUpdatedEvent = NotificationMutesUpdatedEvent(
    type = EventType.NOTIFICATION_MUTES_UPDATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    me = me,
)

public fun randomChannelVisibleEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
    channel: Channel = randomChannel(),
): ChannelVisibleEvent = ChannelVisibleEvent(
    type = EventType.CHANNEL_VISIBLE,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user,
    channel = channel,
)

public fun randomUserStartWatchingEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    watcherCount: Int = randomInt(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
): UserStartWatchingEvent = UserStartWatchingEvent(
    type = EventType.USER_WATCHING_START,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    watcherCount = watcherCount,
    channelType = channelType,
    channelId = channelId,
    user = user,
)

public fun randomChannelHiddenEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
    clearHistory: Boolean = randomBoolean(),
    channel: Channel = randomChannel(),
): ChannelHiddenEvent = ChannelHiddenEvent(
    type = EventType.CHANNEL_HIDDEN,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user,
    channel = channel,
    clearHistory = clearHistory,
)

public fun randomChannelDeletedEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
): ChannelDeletedEvent = ChannelDeletedEvent(
    type = EventType.CHANNEL_DELETED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    channel = channel,
)

public fun randomNotificationChannelDeletedEvent(
    createdAt: Date = Date(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = EventType.NOTIFICATION_CHANNEL_DELETED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomReactionNewEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    reaction: Reaction = randomReaction(),
): ReactionNewEvent {
    return ReactionNewEvent(
        type = EventType.REACTION_NEW,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        reaction = reaction,
    )
}

public fun randomMessageReadEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    lastReadMessageId: String? = randomString(),
): MessageReadEvent {
    return MessageReadEvent(
        type = EventType.MESSAGE_READ,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        lastReadMessageId = lastReadMessageId,
    )
}

public fun randomMessageDeliveredEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    lastDeliveredAt: Date = randomDate(),
    lastDeliveredMessageId: String = randomString(),
) = MessageDeliveredEvent(
    type = EventType.MESSAGE_DELIVERED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    lastDeliveredAt = lastDeliveredAt,
    lastDeliveredMessageId = lastDeliveredMessageId,
)

public fun randomNotificationMarkReadEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    lastReadMessageId: String? = randomString(),
    threadId: String? = randomString(),
    thread: ThreadInfo? = randomThreadInfo(),
    unreadThreads: Int? = randomInt(),
    unreadThreadMessages: Int? = randomInt(),
): NotificationMarkReadEvent = NotificationMarkReadEvent(
    type = EventType.NOTIFICATION_MARK_READ,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    totalUnreadCount = totalUnreadCount,
    unreadChannels = unreadChannels,
    lastReadMessageId = lastReadMessageId,
    threadId = threadId,
    thread = thread,
    unreadThreads = unreadThreads,
    unreadThreadMessages = unreadThreadMessages,
)

public fun randomNotificationMarkUnreadEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    firstUnreadMessageId: String = randomString(),
    lastReadMessageAt: Date = randomDate(),
    lastReadMessageId: String? = randomString(),
    unreadMessages: Int = randomInt(),
    threadId: String? = randomString(),
    unreadThreads: Int = randomInt(),
    unreadThreadMessages: Int = randomInt(),
): NotificationMarkUnreadEvent = NotificationMarkUnreadEvent(
    type = EventType.NOTIFICATION_MARK_UNREAD,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    totalUnreadCount = totalUnreadCount,
    unreadChannels = unreadChannels,
    threadId = threadId,
    unreadThreads = unreadThreads,
    unreadThreadMessages = unreadThreadMessages,
    firstUnreadMessageId = firstUnreadMessageId,
    unreadMessages = unreadMessages,
    lastReadMessageAt = lastReadMessageAt,
    lastReadMessageId = lastReadMessageId,
)

public fun randomTypingStopEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
): TypingStopEvent {
    return TypingStopEvent(
        type = EventType.TYPING_STOP,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId,
    )
}

public fun randomTypingStartEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
): TypingStartEvent {
    return TypingStartEvent(
        type = EventType.TYPING_START,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentId,
    )
}

public fun randomMemberAddedEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    member: Member = randomMember(),
): MemberAddedEvent {
    return MemberAddedEvent(
        type = EventType.MEMBER_ADDED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member,
    )
}

public fun randomNotificationAddedToChannelEvent(
    createdAt: Date = Date(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        member = member,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomNotificationMessageNewEvent(
    createdAt: Date = Date(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    message: Message = randomMessage(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationMessageNewEvent = NotificationMessageNewEvent(
    type = EventType.NOTIFICATION_MESSAGE_NEW,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    channel = channel,
    message = message,
    totalUnreadCount = totalUnreadCount,
    unreadChannels = unreadChannels,
)

public fun randomMessageUpdateEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
): MessageUpdatedEvent = MessageUpdatedEvent(
    type = EventType.MEMBER_UPDATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    message = message,
)

public fun randomChannelUpdatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channel: Channel = randomChannel(),
): ChannelUpdatedEvent {
    return ChannelUpdatedEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
    )
}

public fun randomChannelUpdatedByUserEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channel: Channel = randomChannel(),
    user: User = randomUser(),
): ChannelUpdatedByUserEvent {
    return ChannelUpdatedByUserEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
        user = user,
    )
}

public fun randomNewMessageEvent(
    createdAt: Date = randomDate(),
    user: User = randomUser(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    watcherCount: Int = randomInt(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelMessageCount: Int? = positiveRandomInt(),
): NewMessageEvent {
    return NewMessageEvent(
        type = EventType.MESSAGE_NEW,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        watcherCount = watcherCount,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
        channelMessageCount = channelMessageCount,
    )
}

public fun randomNotificationChannelTruncatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationChannelTruncatedEvent = NotificationChannelTruncatedEvent(
    type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    channel = channel,
    totalUnreadCount = totalUnreadCount,
    unreadChannels = unreadChannels,
)

public fun randomMarkAllReadEvent(
    createdAt: Date = randomDate(),
    user: User = randomUser(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): MarkAllReadEvent = MarkAllReadEvent(
    type = EventType.NOTIFICATION_MARK_READ,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    totalUnreadCount = totalUnreadCount,
    unreadChannels = unreadChannels,
)

public fun randomReminderCreatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    reminder: MessageReminder = randomMessageReminder(),
    messageId: String = randomString(),
    userId: String = randomString(),
): ReminderCreatedEvent = ReminderCreatedEvent(
    type = EventType.REMINDER_CREATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    reminder = reminder,
    messageId = messageId,
    userId = userId,
)

public fun randomReminderUpdatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    reminder: MessageReminder = randomMessageReminder(),
    messageId: String = randomString(),
    userId: String = randomString(),
): ReminderUpdatedEvent = ReminderUpdatedEvent(
    type = EventType.REMINDER_UPDATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    reminder = reminder,
    messageId = messageId,
    userId = userId,
)

public fun randomReminderDeletedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    reminder: MessageReminder = randomMessageReminder(),
    messageId: String = randomString(),
    userId: String = randomString(),
): ReminderDeletedEvent = ReminderDeletedEvent(
    type = EventType.REMINDER_DELETED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    reminder = reminder,
    messageId = messageId,
    userId = userId,
)

public fun randomNotificationReminderDueEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    reminder: MessageReminder = randomMessageReminder(),
    messageId: String = randomString(),
    userId: String = randomString(),
): NotificationReminderDueEvent = NotificationReminderDueEvent(
    type = EventType.NOTIFICATION_REMINDER_DUE,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    reminder = reminder,
    messageId = messageId,
    userId = userId,
)

public fun randomQueryChannelsSpec(
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySorter<Channel> = QuerySortByField(),
    cids: Set<String> = emptySet(),
): QueryChannelsSpec = QueryChannelsSpec(filter, sort).apply { this.cids = cids }

public fun randomNotificationRemovedFromChannelEvent(
    cid: String = randomCID(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
): NotificationRemovedFromChannelEvent {
    val createdAt = Date()

    return NotificationRemovedFromChannelEvent(
        type = randomString(),
        user = randomUser(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        member = member,
    )
}

public fun randomMemberRemovedEvent(
    createdAt: Date = Date(),
    cid: String = randomCID(),
    member: Member = randomMember(),
    user: User = randomUser(),
    channelType: String = randomString(),
    channelId: String = randomString(),
): MemberRemovedEvent = MemberRemovedEvent(
    type = EventType.MEMBER_REMOVED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    member = member,
)

public fun randomChannelUserBannedEvent(
    cid: String = randomCID(),
    user: User = randomUser(),
    createdAt: Date = Date(),
    banExpires: Date? = null,
    shadow: Boolean = false,
): ChannelUserBannedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return ChannelUserBannedEvent(
        type = EventType.USER_BANNED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = type,
        channelId = id,
        expiration = banExpires,
        shadow = shadow,
    )
}

public fun randomUserMessagesDeletedEvent(
    createdAt: Date = randomDate(),
    user: User = randomUser(),
    cid: String? = randomCID(),
    hardDelete: Boolean = randomBoolean(),
): UserMessagesDeletedEvent {
    val (type, id) = cid?.cidToTypeAndId() ?: (null to null)
    return UserMessagesDeletedEvent(
        type = EventType.USER_MESSAGES_DELETED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = type,
        channelId = id,
        hardDelete = hardDelete,
    )
}

public fun randomPollDeletedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
): PollDeletedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return PollDeletedEvent(
        type = EventType.POLL_DELETED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
    )
}

public fun randomPollUpdatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
): PollUpdatedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return PollUpdatedEvent(
        type = EventType.POLL_UPDATED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
    )
}

public fun randomPollClosedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
): PollClosedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return PollClosedEvent(
        type = EventType.POLL_CLOSED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
    )
}

public fun randomVoteCastedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
    newVote: Vote = randomPollVote(),
): VoteCastedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return VoteCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
        newVote = newVote,
    )
}

public fun randomAnswerCastedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
    newAnswer: Answer = randomPollAnswer(),
): AnswerCastedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return AnswerCastedEvent(
        type = EventType.POLL_VOTE_CASTED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
        newAnswer = newAnswer,
    )
}

public fun randomVoteChangedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
    newVote: Vote = randomPollVote(),
): VoteChangedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return VoteChangedEvent(
        type = EventType.POLL_VOTE_CHANGED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
        newVote = newVote,
    )
}

public fun randomVoteRemovedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    messageId: String = randomString(),
    poll: Poll = randomPoll(),
    removedVote: Vote = randomPollVote(),
): VoteRemovedEvent {
    val (type, id) = cid.cidToTypeAndId()
    return VoteRemovedEvent(
        type = EventType.POLL_VOTE_REMOVED,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = type,
        channelId = id,
        messageId = messageId,
        poll = poll,
        removedVote = removedVote,
    )
}
