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

package io.getstream.chat.android.client.test.utils

import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomString
import io.getstream.result.Result
import java.util.Calendar
import java.util.Date
import java.util.UUID
import io.getstream.chat.android.client.events.MessageDeletedEvent as MessageDeletedEvent1

@Suppress("LargeClass")
public class TestDataHelper {

    private val streamFormatter = StreamDateFormatter()

    public val connection1: String = "test-connection"
    public val user1: User = User("broad-lake-3")
    public val user3: User = User("user-3")
    public val userEvil: User = User("user-evil")
    public val mute1: Mute = Mute(user1, userEvil, Date(), Date(), null)
    public val me1: User = User("broad-lake-3", mutes = listOf(mute1))

    public val user1Token: String = randomString()

    public val user2: User = User("test-user-2")
    public val userMap: MutableMap<String, User> = mutableMapOf("broad-lake-3" to user1, "test-user-2" to user2)
    public val user1updated: User = User("broad-lake-3", extraData = mapOf("color" to "green"))

    public val filter1: FilterObject =
        Filters.and(Filters.eq("type", "messaging"), Filters.`in`("members", listOf(user1.id)))

    public val filter2: FilterObject =
        Filters.and(Filters.eq("type", "livestream"), Filters.`in`("members", listOf(user1.id)))

    public val query1: QueryChannelsSpec = QueryChannelsSpec(filter1, QuerySortByField())

    public val attachment1: Attachment =
        Attachment(type = "image", extraData = mapOf("color" to "green"))

    public val watcher1: User = user1
    public val member1: Member = Member(user = user1, isInvited = false)
    public val member2: Member = Member(user = user2, isInvited = false)
    public val member3: Member = Member(user = user3, isInvited = false)

    public val parentMessageId: String = "parentMessageId"

    public fun getOldDate(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 1988)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }

    public val extraData1: MutableMap<String, Any> = mutableMapOf("color" to "green", "score" to 1.1)

    public val config1: Config = Config(typingEventsEnabled = true, readEventsEnabled = true)
    public val channel1: Channel = Channel(
        type = "messaging",
        id = "123-testing",
        watcherCount = 100,
        createdBy = user1,
        watchers = listOf(watcher1),
        members = listOf(member1),
        memberCount = 100,
        config = config1,
    )

    public val channel1WithNewMember: Channel = Channel(
        type = "messaging",
        id = "123-testing",
        watcherCount = 100,
        createdBy = user1,
        watchers = listOf(watcher1),
        members = listOf(member1, member2),
        config = config1,
    )

    public val channel1Updated: Channel = Channel(
        type = "messaging",
        id = "123-testing",
        watcherCount = 100,
        createdBy = user1,
        watchers = listOf(watcher1),
        members = listOf(member1),
        extraData = mutableMapOf("color" to "green"),
        config = config1,
    )

    public val channel2: Channel = Channel(
        type = "messaging",
        id = "222-testing",
        watcherCount = 22,
        createdBy = user1,
        watchers = listOf(watcher1),
        members = listOf(member1),
        config = config1,
    )

    public val channel3: Channel = Channel(
        type = "messaging",
        id = "333-testing",
        watcherCount = 232,
        watchers = listOf(watcher1),
        members = listOf(member1),
        config = config1,
        hidden = false,
    )

    public val channel4: Channel = Channel(
        type = "messaging",
        id = "444-testing",
        watcherCount = 444,
        watchers = listOf(watcher1),
        members = listOf(member1, member2),
        config = config1,
    )

    public val channel5: Channel = Channel(
        type = "messaging",
        id = "555-testing",
        watcherCount = 444,
        watchers = listOf(watcher1),
        members = listOf(member1, member2),
        config = config1,
    )

    public val reaction1: Reaction = Reaction(
        messageId = "message-1",
        type = "like",
        user = user1,
        userId = user1.id,
        score = 10,
    )
    public val reaction2: Reaction = Reaction(
        messageId = "message-1",
        type = "like",
        score = 1,
        user = user2,
    )

    public val message1: Message = Message(
        cid = channel1.cid,
        text = "hi there",
        id = "message-1",
        user = user1,
        createdAt = calendar(2020, 1, 1),
    )

    public val message1WithoutChannelAndCid: Message = Message(
        text = "hi there",
        id = "message -1",
        user = user1,
        createdAt = calendar(2020, 1, 1),
    )

    public fun createMessage(): Message {
        val messageId = UUID.randomUUID().toString()
        val text = "hi there $messageId"
        return Message(
            cid = channel1.cid,
            text = text,
            id = messageId,
            user = user1,
            createdAt = Date(),
        )
    }

    public val message1Updated: Message = Message(
        cid = channel1.cid,
        text = "im update now",
        id = "message-1",
        user = user1,
        createdAt = calendar(2020, 1, 1),
    )

    public val message1Deleted: Message = message1.copy(deletedAt = Date())

    public val reactionMessage1: Message = Message(
        text = "im update now",
        id = "message-1",
        user = user1,
        cid = channel1.cid,
        reactionScores = mutableMapOf("like" to 10),
        reactionCounts = mutableMapOf("like" to 1),
        ownReactions = mutableListOf(reaction1),
        latestReactions = mutableListOf(reaction1),
    )

    public val reactionMessage2: Message = Message(
        text = "im update now",
        id = "message-1",
        user = user1,
        cid = channel1.cid,
        reactionScores = mutableMapOf("like" to 11),
        reactionCounts = mutableMapOf("like" to 2),
        ownReactions = mutableListOf(reaction2),
        latestReactions = mutableListOf(reaction2, reaction1),
    )
    public val message2Older: Message = Message(
        text = "message2",
        id = "message-2",
        user = user1,
        createdAt = calendar(2019, 1, 1),
    )
    public val messageFromUser2: Message = Message(
        text = "messageFromUser2",
        id = "message-2",
        user = user2,
        createdAt = calendar(2020, 2, 1),
    )

    public val connectedEvent: ConnectedEvent by lazy {
        val createdAt = Date()

        ConnectedEvent(EventType.HEALTH_CHECK, createdAt, streamFormatter.format(createdAt), user1, connection1)
    }
    public val connectedEvent2: ConnectedEvent by lazy {
        val createdAt = Date()

        ConnectedEvent(EventType.HEALTH_CHECK, Date(), streamFormatter.format(createdAt), user1, connection1)
    }

    public val disconnectedEvent: DisconnectedEvent = DisconnectedEvent(
        EventType.CONNECTION_DISCONNECTED,
        Date(),
        null,
    )
    public val newMessageEvent: NewMessageEvent by lazy {
        val createdAt = Date()

        NewMessageEvent(
            type = EventType.MESSAGE_NEW,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = message1,
            watcherCount = 1,
            totalUnreadCount = 0,
            unreadChannels = 0,
            channelMessageCount = 1,
        )
    }

    private val newMessageEvent2: NewMessageEvent by lazy {
        val createdAt = Date()

        NewMessageEvent(
            type = EventType.MESSAGE_NEW,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = message2Older,
            watcherCount = 1,
            totalUnreadCount = 0,
            unreadChannels = 0,
            channelMessageCount = 1,
        )
    }
    public val newMessageFromUser2: NewMessageEvent by lazy {
        val createdAt = Date()

        NewMessageEvent(
            type = EventType.MESSAGE_NEW,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user2,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = messageFromUser2,
            watcherCount = 1,
            totalUnreadCount = 0,
            unreadChannels = 0,
            channelMessageCount = 1,
        )
    }

    public val newMessageEventNotification: NotificationMessageNewEvent by lazy {
        val createdAt = Date()

        NotificationMessageNewEvent(
            type = EventType.NOTIFICATION_MESSAGE_NEW,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            channel = channel1,
            message = message1WithoutChannelAndCid,
            totalUnreadCount = 0,
            unreadChannels = 0,
        )
    }

    public val messageUpdatedEvent: MessageUpdatedEvent by lazy {
        val createdAt = Date()

        MessageUpdatedEvent(
            type = EventType.MESSAGE_UPDATED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = message1Updated,
        )
    }

    public val messageDeletedEvent: MessageDeletedEvent1 by lazy {
        val createdAt = Date()

        MessageDeletedEvent1(
            type = EventType.MESSAGE_DELETED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = message1Deleted,
            hardDelete = false,
            channelMessageCount = 1,
        )
    }

    public val messageHardDeletedEvent: MessageDeletedEvent1 by lazy {
        val createdAt = Date()

        MessageDeletedEvent1(
            type = EventType.MESSAGE_DELETED,
            createdAt = Date(),
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = message1Deleted,
            hardDelete = true,
            channelMessageCount = 1,
        )
    }

    public val userStartWatchingEvent: UserStartWatchingEvent by lazy {
        val createdAt = Date()

        UserStartWatchingEvent(
            type = EventType.USER_WATCHING_START,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            watcherCount = 1,
            channelType = channel1.type,
            channelId = channel1.id,
            user = user1,
        )
    }
    public val reactionEvent: ReactionNewEvent by lazy {
        val createdAt = Date()

        ReactionNewEvent(
            type = EventType.REACTION_NEW,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = reactionMessage1,
            reaction = reaction1,
        )
    }
    public val reactionEvent2: ReactionNewEvent by lazy {
        val createdAt = Date()

        ReactionNewEvent(
            type = EventType.REACTION_NEW,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user2,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            message = reactionMessage2,
            reaction = reaction2,
        )
    }

    public val channelUpdatedEvent: ChannelUpdatedEvent by lazy {
        val createdAt = Date()

        ChannelUpdatedEvent(
            type = EventType.CHANNEL_UPDATED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1Updated.cid,
            channelType = channel1Updated.type,
            channelId = channel1Updated.id,
            channel = channel1Updated,
            message = null,
        )
    }
    public val channelUpdatedEvent2: ChannelUpdatedEvent by lazy {
        val createdAt = Date()

        ChannelUpdatedEvent(
            type = EventType.CHANNEL_UPDATED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel5.cid,
            channelType = channel5.type,
            channelId = channel5.id,
            channel = channel5,
            message = null,
        )
    }

    public val user1TypingStarted: TypingStartEvent by lazy {
        val createdAt = Date()

        TypingStartEvent(
            type = EventType.TYPING_START,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            parentId = parentMessageId,
        )
    }
    public val user3TypingStartedOld: TypingStartEvent by lazy {
        val createdAt = getOldDate()

        TypingStartEvent(
            type = EventType.TYPING_START,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user3,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            parentId = parentMessageId,
        )
    }

    public val channelHiddenEvent: ChannelHiddenEvent by lazy {
        val createdAt = Date()

        ChannelHiddenEvent(
            type = EventType.CHANNEL_HIDDEN,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            user = user1,
            channel = channel2,
            clearHistory = false,
        )
    }
    public val channelVisibleEvent: ChannelVisibleEvent by lazy {
        val createdAt = Date()

        ChannelVisibleEvent(
            type = EventType.CHANNEL_VISIBLE,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            channel = channel2,
            user = user1,
        )
    }

    public val user2TypingStarted: TypingStartEvent by lazy {
        val createdAt = Date()

        TypingStartEvent(
            type = EventType.TYPING_START,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user2,
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            parentId = parentMessageId,
        )
    }
    public val user1TypingStop: TypingStopEvent by lazy {
        val createdAt = Date()

        TypingStopEvent(
            type = EventType.TYPING_STOP,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            parentId = parentMessageId,
        )
    }
    public val readEvent: MessageReadEvent by lazy {
        val createdAt = Date()

        MessageReadEvent(
            type = EventType.MESSAGE_READ,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            lastReadMessageId = randomString(),
        )
    }

    public val notificationMutesUpdated: NotificationMutesUpdatedEvent by lazy {
        val createdAt = Date()

        NotificationMutesUpdatedEvent(
            type = EventType.NOTIFICATION_MUTES_UPDATED,
            createdAt = Date(),
            rawCreatedAt = streamFormatter.format(createdAt),
            me = me1,
        )
    }

    public val user1Banned: ChannelUserBannedEvent by lazy {
        val createdAt = Date()

        ChannelUserBannedEvent(
            type = EventType.USER_BANNED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            user = user1,
            expiration = null,
            shadow = false,
        )
    }
    public val user1Unbanned: ChannelUserUnbannedEvent by lazy {
        val createdAt = Date()

        ChannelUserUnbannedEvent(
            type = EventType.USER_UNBANNED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
        )
    }

    public val user1ReadNotification: NotificationMarkReadEvent by lazy {
        val createdAt = Date()

        NotificationMarkReadEvent(
            type = EventType.NOTIFICATION_MARK_READ,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            totalUnreadCount = 0,
            unreadChannels = 0,
            lastReadMessageId = randomString(),
        )
    }
    public val user1Read: MessageReadEvent by lazy {
        val createdAt = Date()

        MessageReadEvent(
            type = EventType.MESSAGE_READ,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            lastReadMessageId = randomString(),
        )
    }
    public val memberAddedToChannelEvent: MemberAddedEvent by lazy {
        val createdAt = Date()

        MemberAddedEvent(
            type = EventType.MEMBER_ADDED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = user1,
            cid = channel1WithNewMember.cid,
            channelType = channel1WithNewMember.type,
            channelId = channel1WithNewMember.id,
            member = member2,
        )
    }

    // member removed doesn't have a cid
    public val memberRemovedFromChannel: MemberRemovedEvent by lazy {
        val createdAt = Date()

        MemberRemovedEvent(
            type = EventType.MEMBER_REMOVED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            user = member2.user,
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            member = member1,
        )
    }

    public val notificationRemovedFromChannel: NotificationRemovedFromChannelEvent by lazy {
        val createdAt = Date()

        NotificationRemovedFromChannelEvent(
            type = EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            channel = channel1,
            member = member1,
            user = user1,
        )
    }

    // for whatever reason these events don't have event.cid
    public val notificationAddedToChannelEvent: NotificationAddedToChannelEvent by lazy {
        val createdAt = Date()

        NotificationAddedToChannelEvent(
            type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            channel = channel1,
            member = member1,
            totalUnreadCount = 0,
            unreadChannels = 0,
        )
    }
    public val notificationAddedToChannel2Event: NotificationAddedToChannelEvent by lazy {
        val createdAt = Date()

        NotificationAddedToChannelEvent(
            type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel2.cid,
            channelType = channel2.type,
            channelId = channel2.id,
            channel = channel2,
            member = member2,
            totalUnreadCount = 0,
            unreadChannels = 0,
        )
    }

    // no created by
    public val notificationAddedToChannel3Event: NotificationAddedToChannelEvent by lazy {
        val createdAt = Date()

        NotificationAddedToChannelEvent(
            type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel3.cid,
            channelType = channel3.type,
            channelId = channel3.id,
            channel = channel3,
            member = member3,
            totalUnreadCount = 0,
            unreadChannels = 0,
        )
    }
    public val user1UpdatedEvent: UserUpdatedEvent by lazy {
        val createdAt = Date()

        UserUpdatedEvent(EventType.USER_UPDATED, createdAt, streamFormatter.format(createdAt), user1updated)
    }
    public val syncHistoryResult: Result<List<ChatEvent>> =
        Result.Success(listOf(notificationAddedToChannelEvent, newMessageEvent, newMessageEvent2))

    public val channelTruncatedEvent: ChannelTruncatedEvent by lazy {
        val createdAt = Date()

        ChannelTruncatedEvent(
            type = EventType.CHANNEL_TRUNCATED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            channel = channel1,
            user = user1,
            message = null,
        )
    }
    public val notificationChannelTruncated: NotificationChannelTruncatedEvent by lazy {
        val createdAt = Date()

        NotificationChannelTruncatedEvent(
            type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            channel = channel1,
        )
    }
    public val channelDeletedEvent: ChannelDeletedEvent by lazy {
        val createdAt = Date()

        ChannelDeletedEvent(
            type = EventType.CHANNEL_DELETED,
            createdAt = createdAt,
            rawCreatedAt = streamFormatter.format(createdAt),
            cid = channel1.cid,
            channelType = channel1.type,
            channelId = channel1.id,
            channel = channel1,
            user = null,
        )
    }
}

public fun calendar(
    year: Int,
    month: Int,
    date: Int,
    hourOfDay: Int = 0,
    minute: Int = 0,
    seconds: Int = 0,
): Date {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, date, hourOfDay, minute, seconds)
    return calendar.time
}
