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
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
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

public fun randomChannelVisibleEvent(
    createdAt: Date = randomDate(),
    cid: String = randomCID(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    user: User = randomUser(),
): ChannelVisibleEvent = ChannelVisibleEvent(
    type = EventType.CHANNEL_VISIBLE,
    createdAt = createdAt,
    rawCreatedAt = streamFormatter.format(createdAt),
    cid = cid,
    channelType = channelType,
    channelId = channelId,
    user = user,
)

public fun randomUserStartWatchingEvent(
    createdAt: Date = randomDate(),
    cid: String = randomString(),
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

public fun randomChannelDeletedEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
): ChannelDeletedEvent {
    return ChannelDeletedEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
    )
}

public fun randomNotificationChannelDeletedEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationChannelDeletedEvent {
    return NotificationChannelDeletedEvent(
        type = type,
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
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    reaction: Reaction = randomReaction(),
): ReactionNewEvent {
    return ReactionNewEvent(
        type = type,
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
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
): MessageReadEvent {
    return MessageReadEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
    )
}

public fun randomNotificationMarkReadEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationMarkReadEvent {
    return NotificationMarkReadEvent(
        type = type,
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
}

public fun randomTypingStopEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
): TypingStopEvent {
    return TypingStopEvent(
        type = type,
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
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    parentId: String? = randomString(),
): TypingStartEvent {
    return TypingStartEvent(
        type = type,
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
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    member: Member = randomMember(),
): MemberAddedEvent {
    return MemberAddedEvent(
        type = type,
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
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationAddedToChannelEvent {
    return NotificationAddedToChannelEvent(
        type = type,
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
    type: String = randomString(),
    createdAt: Date = Date(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    channel: Channel = randomChannel(),
    message: Message = randomMessage(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
): NotificationMessageNewEvent {
    return NotificationMessageNewEvent(
        type = type,
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
}

public fun randomMessageUpdateEvent(
    type: String = randomString(),
    createdAt: Date = Date(),
    user: User = randomUser(),
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
): MessageUpdatedEvent = MessageUpdatedEvent(
    type = type,
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
    cid: String = randomString(),
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
    cid: String = randomString(),
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
    cid: String = randomString(),
    channelType: String = randomString(),
    channelId: String = randomString(),
    message: Message = randomMessage(),
    watcherCount: Int = randomInt(),
    totalUnreadCount: Int = randomInt(),
    unreadChannels: Int = randomInt(),
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
    )
}

public fun randomQueryChannelsSpec(
    filter: FilterObject = NeutralFilterObject,
    sort: QuerySorter<Channel> = QuerySortByField(),
    cids: Set<String> = emptySet(),
): QueryChannelsSpec = QueryChannelsSpec(filter, sort).apply { this.cids = cids }

public fun randomNotificationAddedToChannelEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel(),
    member: Member = randomMember(),
): NotificationAddedToChannelEvent {
    val createdAt = Date()

    return NotificationAddedToChannelEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        member = member,
        totalUnreadCount = randomInt(),
        unreadChannels = randomInt(),
    )
}

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
    )
}

public fun randomNotificationMessageNewEvent(
    cid: String = randomString(),
    channel: Channel = randomChannel(),
): NotificationMessageNewEvent {
    val createdAt = Date()

    return NotificationMessageNewEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        channel = channel,
        message = randomMessage(),
        totalUnreadCount = randomInt(),
        unreadChannels = randomInt(),
    )
}

public fun randomMemberAddedEvent(cid: String = randomString()): MemberAddedEvent {
    val createdAt = Date()

    return MemberAddedEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = randomUser(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        member = randomMember(),
    )
}

public fun randomMemberRemovedEvent(cid: String = randomString(), member: Member = randomMember()): MemberRemovedEvent {
    val createdAt = Date()

    return MemberRemovedEvent(
        type = randomString(),
        createdAt = createdAt,
        rawCreatedAt = streamFormatter.format(createdAt),
        user = randomUser(),
        cid = cid,
        channelType = randomString(),
        channelId = randomString(),
        member = member,
    )
}

public fun randomChannelUserBannedEvent(
    cid: String = randomCID(),
    user: User = randomUser(),
    createdAt: Date = Date(),
    banExpires: Date? = null,
    shadow: Boolean = false,
): ChannelUserBannedEvent {

    val (type, id) = cid.cidToTypeAndId()
    return ChannelUserBannedEvent(
        type = randomString(),
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