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
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.NeutralFilterObject
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomReaction
import io.getstream.chat.android.randomString
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
    channelLastMessageAt: Date = randomDate(),

): ChannelVisibleEvent = ChannelVisibleEvent(
    type = EventType.CHANNEL_VISIBLE,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user,
    channelLastMessageAt = channelLastMessageAt,
)

public fun randomUserStartWatchingEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    watcherCount: Int = randomInt(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
    channelLastMessageAt: Date = randomDate(),
): UserStartWatchingEvent = UserStartWatchingEvent(
    type = EventType.USER_WATCHING_START,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    watcherCount = watcherCount,
    channelType = channelType,
    channelId = channelId,
    user = user,
    channelLastMessageAt = channelLastMessageAt,
)

public fun randomChannelHiddenEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
    channelLastMessageAt: Date = randomDate(),
    clearHistory: Boolean = randomBoolean(),
): ChannelHiddenEvent = ChannelHiddenEvent(
    type = EventType.CHANNEL_HIDDEN,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user,
    channelLastMessageAt = channelLastMessageAt,
    clearHistory = clearHistory,
)

public fun randomChannelDeletedEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    channelLastMessageAt: Date = randomDate(),
): ChannelDeletedEvent = ChannelDeletedEvent(
    type = EventType.CHANNEL_DELETED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    channel = channel,
    channelLastMessageAt = channelLastMessageAt,
)

public fun randomNotificationChannelDeletedEvent(
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomReactionNewEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    reaction: Reaction = randomReaction(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomMessageReadEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channelLastMessageAt: Date = randomDate(),
): MessageReadEvent {
    return MessageReadEvent(
        type = EventType.MESSAGE_READ,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomNotificationMarkReadEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
    threadId: String? = randomString(),
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
    channelLastMessageAt = channelLastMessageAt,
    threadId = threadId,
    unreadThreads = unreadThreads,
    unreadThreadMessages = unreadThreadMessages,
)
public fun randomNotificationMarkUnreadEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
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
    channelLastMessageAt = channelLastMessageAt,
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
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomTypingStartEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomMemberAddedEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    member: Member = randomMember(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomNotificationAddedToChannelEvent(
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomNotificationMessageNewEvent(
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    message: Message = randomMessage(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
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
    channelLastMessageAt = channelLastMessageAt,
)

public fun randomMessageUpdateEvent(
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channelLastMessageAt: Date = randomDate(),
): MessageUpdatedEvent = MessageUpdatedEvent(
    type = EventType.MEMBER_UPDATED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    message = message,
    channelLastMessageAt = channelLastMessageAt,
)

public fun randomChannelUpdatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channel: Channel = randomChannel(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomChannelUpdatedByUserEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    channel: Channel = randomChannel(),
    user: User = randomUser(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomNewMessageEvent(
    createdAt: Date = randomDate(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    watcherCount: Int = randomInt(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}

public fun randomNotificationChannelTruncatedEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
    channelLastMessageAt: Date = randomDate(),
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
    channelLastMessageAt = channelLastMessageAt,
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

public fun randomQueryChannelsSpec(
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySorter<Channel> = QuerySortByField(),
    cids: Set<String> = emptySet(),
): QueryChannelsSpec = QueryChannelsSpec(filter, sort).apply { this.cids = cids }

public fun randomNotificationRemovedFromChannelEvent(
    cid: String = randomString(),
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
        channelLastMessageAt = randomDate(),
    )
}

public fun randomMemberRemovedEvent(
    createdAt: Date = Date(),
    cid: String = randomString(),
    member: Member = randomMember(),
    user: User = randomUser(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channelLastMessageAt: Date = randomDate(),
): MemberRemovedEvent = MemberRemovedEvent(
    type = EventType.MEMBER_REMOVED,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    user = user,
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    member = member,
    channelLastMessageAt = channelLastMessageAt,
)

public fun randomChannelUserBannedEvent(
    cid: String = randomCID(),
    user: User = randomUser(),
    createdAt: Date = Date(),
    banExpires: Date? = null,
    shadow: Boolean = false,
    channelLastMessageAt: Date = randomDate(),
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
        channelLastMessageAt = channelLastMessageAt,
    )
}
