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

package io.getstream.chat.android.offline.utils

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
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
import io.getstream.chat.android.client.events.MessageDeletedEvent
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
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Mute
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.utils.Result
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import java.util.Calendar
import java.util.Date
import java.util.UUID

public class TestDataHelper {
    public val dotenv: Dotenv = dotenv {
        ignoreIfMissing = true
    }

    public val apiKey: String = checkNotNull(dotenv["STREAM_API_KEY"]) { "Be sure to specify the STREAM_API_KEY environment variable" }
    public val logLevel: String =
        checkNotNull(dotenv["STREAM_LOG_LEVEL"]) { "Be sure to specify the STREAM_LOG_LEVEL environment variable" }

    public val connection1: String = "test-connection"
    public val user1: User = User("broad-lake-3")
    public val user3: User = User("user-3")
    public val userEvil: User = User("user-evil")
    public val mute1: Mute = Mute(user1, userEvil, Date(), Date(), null)
    public val me1: User = User("broad-lake-3").apply { mutes = listOf(mute1) }

    public val user1Token: String =
        checkNotNull(dotenv["STREAM_USER_1_TOKEN"]) { "Be sure to specify the STREAM_USER_1_TOKEN environment variable" }

    public val user2: User = User("test-user-2")
    public val userMap: MutableMap<String, User> = mutableMapOf("broad-lake-3" to user1, "test-user-2" to user2)
    public val user1updated: User = User("broad-lake-3").apply { extraData = mutableMapOf("color" to "green") }

    public val filter1: FilterObject =
        Filters.and(Filters.eq("type", "messaging"), Filters.`in`("members", listOf(user1.id)))

    public val filter2: FilterObject =
        Filters.and(Filters.eq("type", "livestream"), Filters.`in`("members", listOf(user1.id)))

    public val query1: QueryChannelsSpec = QueryChannelsSpec(filter1, QuerySort())

    public val attachment1: Attachment =
        Attachment(type = "image").apply { extraData = mutableMapOf("color" to "green") }

    public val watcher1: User = user1
    public val member1: Member = Member(user = user1, role = "user", isInvited = false)
    public val member2: Member = Member(user = user2, role = "user", isInvited = false)

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
    public val channel1: Channel = Channel().copy(
        type = "messaging",
        id = "123-testing",
        cid = "messaging:123-testing",
        watcherCount = 100,
        createdBy = user1,
        watchers = listOf(watcher1),
        members = listOf(member1),
        memberCount = 100,
        config = config1
    )

    public val channel1WithNewMember: Channel = Channel().apply {
        type = "messaging"
        id = "123-testing"
        cid = "messaging:123-testing"
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1, member2)
        config = config1
    }

    public val channel1Updated: Channel = Channel().apply {
        type = "messaging"
        id = "123-testing"
        cid = "messaging:123-testing"
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1)
        extraData = mutableMapOf("color" to "green")
        config = config1
    }

    public val channel2: Channel = Channel().apply {
        type = "messaging"
        id = "222-testing"
        cid = "messaging:222-testing"
        watcherCount = 22
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1)
        config = config1
    }

    public val channel3: Channel = Channel().apply {
        type = "messaging"
        id = "333-testing"
        cid = "messaging:333-testing"
        watcherCount = 232
        watchers = listOf(watcher1)
        members = listOf(member1)
        config = config1
        unreadCount = 0
        hidden = false
    }

    public val channel4: Channel = Channel().apply {
        type = "messaging"
        id = "444-testing"
        cid = "messaging:444-testing"
        watcherCount = 444
        watchers = listOf(watcher1)
        members = listOf(member1, member2)
        config = config1
    }

    public val channel5: Channel = Channel().apply {
        type = "messaging"
        id = "555-testing"
        cid = "messaging:555-testing"
        watcherCount = 444
        watchers = listOf(watcher1)
        members = listOf(member1, member2)
        config = config1
    }

    public val reaction1: Reaction = Reaction("message-1", "like", 1).apply { user = user1; userId = user1.id; score = 10 }
    public val reaction2: Reaction = Reaction("message-1", "like", 1).apply { user = user2 }

    public val message1: Message = Message().apply {
        cid = channel1.cid; text = "hi there"; id = "message-1"; user =
            user1; createdAt = calendar(2020, 1, 1)
    }

    public val message1WithoutChannelAndCid: Message = Message().apply {
        text = "hi there"; id = "message-1"; user =
            user1; createdAt = calendar(2020, 1, 1)
    }

    public fun createMessage(): Message {
        val messageId = UUID.randomUUID().toString()
        val text = "hi there $messageId"
        return Message().apply {
            cid = channel1.cid; this.text = text; id = messageId; user =
                user1; createdAt = Date()
        }
    }

    public val message1Updated: Message = Message().apply {
        cid = channel1.cid; text = "im update now"; id = "message-1"; user =
            user1; createdAt = calendar(2020, 1, 1)
    }

    public val message1Deleted: Message = message1.copy(deletedAt = Date())

    public val reactionMessage1: Message = Message().apply {
        text = "im update now"
        id = "message-1"
        user = user1
        cid = channel1.cid
        reactionScores = mutableMapOf("like" to 10)
        reactionCounts = mutableMapOf("like" to 1)
        ownReactions = mutableListOf(reaction1)
        latestReactions = mutableListOf(reaction1)
    }

    public val reactionMessage2: Message = Message().apply {
        text = "im update now"; id = "message-1"; user = user1
        cid = channel1.cid
        reactionScores = mutableMapOf("like" to 11)
        reactionCounts = mutableMapOf("like" to 2)
        ownReactions = mutableListOf(reaction2)
        latestReactions = mutableListOf(reaction2, reaction1)
    }
    public val message2Older: Message = Message().apply {
        text = "message2"; id = "message-2"; user = user1; createdAt =
            calendar(2019, 1, 1)
    }
    public val messageFromUser2: Message = Message().apply {
        text = "messageFromUser2"; id = "message-2"; user = user2; createdAt =
            calendar(2020, 2, 1)
    }

    public val connectedEvent: ConnectedEvent = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user1, connection1)
    public val connectedEvent2: ConnectedEvent = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user1, connection1)

    public val disconnectedEvent: DisconnectedEvent = DisconnectedEvent(
        EventType.CONNECTION_DISCONNECTED, Date()
    )
    public val newMessageEvent: NewMessageEvent = NewMessageEvent(
        EventType.MESSAGE_NEW,
        Date(),
        user1,
        channel1.cid,
        channel1.type,
        channel1.id,
        message1,
        1,
        0,
        0
    )
    public val newMessageEvent2: NewMessageEvent = NewMessageEvent(
        EventType.MESSAGE_NEW,
        Date(),
        user1,
        channel1.cid,
        channel1.type,
        channel1.id,
        message2Older,
        1,
        0,
        0
    )
    public val newMessageFromUser2: NewMessageEvent = NewMessageEvent(
        EventType.MESSAGE_NEW,
        Date(),
        user2,
        channel1.cid,
        channel1.type,
        channel1.id,
        messageFromUser2,
        1,
        0,
        0
    )

    public val newMessageEventNotification: NotificationMessageNewEvent = NotificationMessageNewEvent(
        EventType.NOTIFICATION_MESSAGE_NEW,
        Date(),
        channel1.cid,
        channel1.type,
        channel1.id,
        channel1,
        message1WithoutChannelAndCid,
        0,
        0
    )

    public val messageUpdatedEvent: MessageUpdatedEvent = MessageUpdatedEvent(
        EventType.MESSAGE_UPDATED,
        Date(),
        user1,
        channel1.cid,
        channel1.type,
        channel1.id,
        message1Updated
    )

    public val messageDeletedEvent: MessageDeletedEvent = MessageDeletedEvent(
        type = EventType.MESSAGE_DELETED,
        createdAt = Date(),
        user = user1,
        cid = channel1.cid,
        channelType = channel1.type,
        channelId = channel1.id,
        message = message1Deleted,
        hardDelete = false
    )

    public val messageHardDeletedEvent: MessageDeletedEvent = MessageDeletedEvent(
        type = EventType.MESSAGE_DELETED,
        createdAt = Date(),
        user = user1,
        cid = channel1.cid,
        channelType = channel1.type,
        channelId = channel1.id,
        message = message1Deleted,
        hardDelete = true
    )

    public val userStartWatchingEvent: UserStartWatchingEvent = UserStartWatchingEvent(
        EventType.USER_WATCHING_START,
        Date(),
        channel1.cid,
        1,
        channel1.type,
        channel1.id,
        user1
    )
    public val reactionEvent: ReactionNewEvent = ReactionNewEvent(
        EventType.REACTION_NEW,
        Date(),
        user1,
        channel1.cid,
        channel1.type,
        channel1.id,
        reactionMessage1,
        reaction1
    )
    public val reactionEvent2: ReactionNewEvent = ReactionNewEvent(
        EventType.REACTION_NEW,
        Date(),
        user2,
        channel1.cid,
        channel1.type,
        channel1.id,
        reactionMessage2,
        reaction2
    )

    public val channelUpdatedEvent: ChannelUpdatedEvent = ChannelUpdatedEvent(
        EventType.CHANNEL_UPDATED,
        Date(),
        channel1Updated.cid,
        channel1Updated.type,
        channel1Updated.id,
        null,
        channel1Updated
    )
    public val channelUpdatedEvent2: ChannelUpdatedEvent =
        ChannelUpdatedEvent(EventType.CHANNEL_UPDATED, Date(), channel5.cid, channel5.type, channel5.id, null, channel5)

    public val user1TypingStarted: TypingStartEvent = TypingStartEvent(
        EventType.TYPING_START,
        Date(),
        user1,
        channel1.cid,
        channel1.type,
        channel1.id,
        parentMessageId
    )
    public val user3TypingStartedOld: TypingStartEvent = TypingStartEvent(
        EventType.TYPING_START,
        getOldDate(),
        user3,
        channel1.cid,
        channel1.type,
        channel1.id,
        parentMessageId
    )

    public val channelHiddenEvent: ChannelHiddenEvent =
        ChannelHiddenEvent(EventType.CHANNEL_HIDDEN, Date(), channel2.cid, channel2.type, channel2.id, user1, false)
    public val channelVisibleEvent: ChannelVisibleEvent =
        ChannelVisibleEvent(EventType.CHANNEL_VISIBLE, Date(), channel2.cid, channel2.type, channel2.id, user1)

    public val user2TypingStarted: TypingStartEvent = TypingStartEvent(
        EventType.TYPING_START,
        Date(),
        user2,
        channel2.cid,
        channel2.type,
        channel2.id,
        parentMessageId
    )
    public val user1TypingStop: TypingStopEvent =
        TypingStopEvent(EventType.TYPING_STOP, Date(), user1, channel2.cid, channel2.type, channel2.id, parentMessageId)
    public val readEvent: MessageReadEvent = MessageReadEvent(
        EventType.MESSAGE_READ, Date(), user1, channel1.cid, channel1.type, channel1.id
    )

    public val notificationMutesUpdated: NotificationMutesUpdatedEvent = NotificationMutesUpdatedEvent(
        EventType.NOTIFICATION_MUTES_UPDATED, Date(), me1
    )

    public val user1Banned: ChannelUserBannedEvent =
        ChannelUserBannedEvent(EventType.USER_BANNED, Date(), channel2.cid, channel2.type, channel2.id, user1, null)
    public val user1Unbanned: ChannelUserUnbannedEvent =
        ChannelUserUnbannedEvent(EventType.USER_UNBANNED, Date(), user1, channel2.cid, channel2.type, channel2.id)

    public val user1ReadNotification: NotificationMarkReadEvent = NotificationMarkReadEvent(
        EventType.NOTIFICATION_MARK_READ,
        Date(),
        user1,
        channel2.cid,
        channel2.type,
        channel2.id,
        0,
        0
    )
    public val user1Read: MessageReadEvent = MessageReadEvent(
        EventType.MESSAGE_READ, Date(), user1, channel2.cid, channel2.type, channel2.id
    )
    public val memberAddedToChannelEvent: MemberAddedEvent = MemberAddedEvent(
        EventType.MEMBER_ADDED,
        Date(),
        user1,
        channel1WithNewMember.cid,
        channel1WithNewMember.type,
        channel1WithNewMember.id,
        member2
    )

    // member removed doesn't have a cid
    public val memberRemovedFromChannel: MemberRemovedEvent =
        MemberRemovedEvent(EventType.MEMBER_REMOVED, Date(), member2.user, channel1.cid, channel1.type, channel1.id, member1)

    public val notificationRemovedFromChannel: NotificationRemovedFromChannelEvent =
        NotificationRemovedFromChannelEvent(
            EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
            Date(),
            user1,
            channel1.cid,
            channel1.type,
            channel1.id,
            channel1,
            member1
        )

    // for whatever reason these events don't have event.cid
    public val notificationAddedToChannelEvent: NotificationAddedToChannelEvent = NotificationAddedToChannelEvent(
        EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        Date(),
        channel1.cid,
        channel1.type,
        channel1.id,
        channel1,
        0,
        0
    )
    public val notificationAddedToChannel2Event: NotificationAddedToChannelEvent = NotificationAddedToChannelEvent(
        EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        Date(),
        channel2.cid,
        channel2.type,
        channel2.id,
        channel2,
        0,
        0
    )

    // no created by
    public val notificationAddedToChannel3Event: NotificationAddedToChannelEvent = NotificationAddedToChannelEvent(
        EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        Date(),
        channel3.cid,
        channel3.type,
        channel3.id,
        channel3,
        0,
        0
    )
    public val user1UpdatedEvent: UserUpdatedEvent = UserUpdatedEvent(EventType.USER_UPDATED, Date(), user1updated)
    public val syncHistoryResult: Result<List<ChatEvent>> =
        Result(listOf(notificationAddedToChannelEvent, newMessageEvent, newMessageEvent2))

    public val channelTruncatedEvent: ChannelTruncatedEvent = ChannelTruncatedEvent(
        EventType.CHANNEL_TRUNCATED,
        Date(),
        channel1.cid,
        channel1.type,
        channel1.id,
        user1,
        null,
        channel1
    )
    public val notificationChannelTruncated: NotificationChannelTruncatedEvent = NotificationChannelTruncatedEvent(
        EventType.NOTIFICATION_CHANNEL_TRUNCATED,
        Date(),
        channel1.cid,
        channel1.type,
        channel1.id,
        channel1
    )
    public val channelDeletedEvent: ChannelDeletedEvent =
        ChannelDeletedEvent(EventType.CHANNEL_DELETED, Date(), channel1.cid, channel1.type, channel1.id, channel1, null)
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
