package io.getstream.chat.android.livedata.utils

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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import io.getstream.chat.android.offline.repository.domain.syncState.SyncStateEntity
import io.github.cdimascio.dotenv.dotenv
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.UUID

internal class TestDataHelper {
    val dotenv = dotenv {
        ignoreIfMissing = true
    }

    val apiKey = checkNotNull(dotenv["STREAM_API_KEY"]) { "Be sure to specify the STREAM_API_KEY environment variable" }
    val logLevel = checkNotNull(dotenv["STREAM_LOG_LEVEL"]) { "Be sure to specify the STREAM_LOG_LEVEL environment variable" }

    val connection1 = "test-connection"
    val user1 = User("broad-lake-3")
    val user3 = User("user-3")
    val userEvil = User("user-evil")
    val mute1 = Mute(user1, userEvil, Date(), Date())
    val me1 = User("broad-lake-3").apply { mutes = listOf(mute1) }

    val user1Token = checkNotNull(dotenv["STREAM_USER_1_TOKEN"]) { "Be sure to specify the STREAM_USER_1_TOKEN environment variable" }

    val user2 = User("test-user-2")
    val userMap = mutableMapOf("broad-lake-3" to user1, "test-user-2" to user2)
    val user1updated = User("broad-lake-3").apply { extraData = mutableMapOf("color" to "green") }

    val filter1 =
        Filters.and(Filters.eq("type", "messaging"), Filters.`in`("members", listOf(user1.id)))

    val filter2 =
        Filters.and(Filters.eq("type", "livestream"), Filters.`in`("members", listOf(user1.id)))

    val query1 = QueryChannelsSpec(filter1, QuerySort())

    val attachment1 =
        Attachment(type = "image").apply { extraData = mutableMapOf("color" to "green") }

    val watcher1 = user1
    val member1 = Member(user = user1, role = "user", isInvited = false)
    val member2 = Member(user = user2, role = "user", isInvited = false)

    val parentMessageId = "parentMessageId"

    fun getOldDate(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, 1988)
        cal.set(Calendar.MONTH, Calendar.JANUARY)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.time
    }

    val extraData1: MutableMap<String, Any> = mutableMapOf("color" to "green", "score" to 1.1)

    val config1 = Config(isTypingEvents = true, isReadEvents = true)
    val channel1 = Channel().apply {
        type = "messaging"
        id = "123-testing"
        cid = "messaging:123-testing"
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1)
        config = config1
    }

    val channel1WithNewMember = Channel().apply {
        type = "messaging"
        id = "123-testing"
        cid = "messaging:123-testing"
        watcherCount = 100
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1, member2)
        config = config1
    }

    val channel1Updated = Channel().apply {
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

    val channel2 = Channel().apply {
        type = "messaging"
        id = "222-testing"
        cid = "messaging:222-testing"
        watcherCount = 22
        createdBy = user1
        watchers = listOf(watcher1)
        members = listOf(member1)
        config = config1
    }

    val channel3 = Channel().apply {
        type = "messaging"
        id = "333-testing"
        cid = "messaging:333-testing"
        watcherCount = 232
        watchers = listOf(watcher1)
        members = listOf(member1)
        config = config1
    }

    val channel4 = Channel().apply {
        type = "messaging"
        id = "444-testing"
        cid = "messaging:444-testing"
        watcherCount = 444
        watchers = listOf(watcher1)
        members = listOf(member1, member2)
        config = config1
    }

    val channel5 = Channel().apply {
        type = "messaging"
        id = "555-testing"
        cid = "messaging:555-testing"
        watcherCount = 444
        watchers = listOf(watcher1)
        members = listOf(member1, member2)
        config = config1
    }

    val reaction1 = Reaction("message-1", "like", 1).apply { user = user1; userId = user1.id; score = 10 }
    val reaction2 = Reaction("message-1", "like", 1).apply { user = user2 }

    val message1 = Message().apply {
        cid = channel1.cid; text = "hi there"; id = "message-1"; user =
            user1; createdAt = calendar(2020, 1, 1)
    }

    val message1WithoutChannelAndCid = Message().apply {
        text = "hi there"; id = "message-1"; user =
            user1; createdAt = calendar(2020, 1, 1)
    }

    fun createMessage(): Message {
        val messageId = UUID.randomUUID().toString()
        val text = "hi there $messageId"
        return Message().apply {
            cid = channel1.cid; this.text = text; id = messageId; user =
                user1; createdAt = Date()
        }
    }

    val message1Updated = Message().apply {
        cid = channel1.cid; text = "im update now"; id = "message-1"; user =
            user1; createdAt = calendar(2020, 1, 1)
    }
    val reactionMessage1 = Message().apply {
        text = "im update now"
        id = "message-1"
        user = user1
        cid = channel1.cid
        reactionScores = mutableMapOf("like" to 10)
        reactionCounts = mutableMapOf("like" to 1)
        ownReactions = mutableListOf(reaction1)
        latestReactions = mutableListOf(reaction1)
    }

    val reactionMessage2 = Message().apply {
        text = "im update now"; id = "message-1"; user = user1
        cid = channel1.cid
        reactionScores = mutableMapOf("like" to 11)
        reactionCounts = mutableMapOf("like" to 2)
        ownReactions = mutableListOf(reaction2)
        latestReactions = mutableListOf(reaction2, reaction1)
    }
    val message2Older = Message().apply {
        text = "message2"; id = "message-2"; user = user1; createdAt =
            calendar(2019, 1, 1)
    }
    val messageFromUser2 = Message().apply {
        text = "messageFromUser2"; id = "message-2"; user = user2; createdAt =
            calendar(2020, 2, 1)
    }

    val connectedEvent = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user1, connection1)
    val connectedEvent2 = ConnectedEvent(EventType.HEALTH_CHECK, Date(), user1, connection1)

    val disconnectedEvent = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
    val newMessageEvent = NewMessageEvent(EventType.MESSAGE_NEW, Date(), user1, channel1.cid, channel1.type, channel1.id, message1, null, null, null)
    val newMessageEvent2 = NewMessageEvent(EventType.MESSAGE_NEW, Date(), user1, channel1.cid, channel1.type, channel1.id, message2Older, null, null, null)
    val newMessageFromUser2 = NewMessageEvent(EventType.MESSAGE_NEW, Date(), user2, channel1.cid, channel1.type, channel1.id, messageFromUser2, null, null, null)

    val newMessageEventNotification = NotificationMessageNewEvent(EventType.NOTIFICATION_MESSAGE_NEW, Date(), channel1.cid, channel1.type, channel1.id, channel1, message1WithoutChannelAndCid, null, null, null)

    val messageUpdatedEvent = MessageUpdatedEvent(EventType.MESSAGE_UPDATED, Date(), user1, channel1.cid, channel1.type, channel1.id, message1Updated, null)
    val userStartWatchingEvent = UserStartWatchingEvent(EventType.USER_WATCHING_START, Date(), channel1.cid, 1, channel1.type, channel1.id, user1)
    val reactionEvent = ReactionNewEvent(EventType.REACTION_NEW, Date(), user1, channel1.cid, channel1.type, channel1.id, reactionMessage1, reaction1)
    val reactionEvent2 = ReactionNewEvent(EventType.REACTION_NEW, Date(), user2, channel1.cid, channel1.type, channel1.id, reactionMessage2, reaction2)

    val channelUpdatedEvent = ChannelUpdatedEvent(EventType.CHANNEL_UPDATED, Date(), channel1Updated.cid, channel1Updated.type, channel1Updated.id, null, channel1Updated)
    val channelUpdatedEvent2 = ChannelUpdatedEvent(EventType.CHANNEL_UPDATED, Date(), channel5.cid, channel5.type, channel5.id, null, channel5)

    val user1TypingStarted = TypingStartEvent(EventType.TYPING_START, Date(), user1, channel1.cid, channel1.type, channel1.id, parentMessageId)
    val user3TypingStartedOld = TypingStartEvent(EventType.TYPING_START, getOldDate(), user3, channel1.cid, channel1.type, channel1.id, parentMessageId)

    val channelHiddenEvent = ChannelHiddenEvent(EventType.CHANNEL_HIDDEN, Date(), channel2.cid, channel2.type, channel2.id, user1, false)
    val channelVisibleEvent = ChannelVisibleEvent(EventType.CHANNEL_VISIBLE, Date(), channel2.cid, channel2.type, channel2.id, user1)

    val user2TypingStarted = TypingStartEvent(EventType.TYPING_START, Date(), user2, channel2.cid, channel2.type, channel2.id, parentMessageId)
    val user1TypingStop = TypingStopEvent(EventType.TYPING_STOP, Date(), user1, channel2.cid, channel2.type, channel2.id, parentMessageId)
    val readEvent = MessageReadEvent(EventType.MESSAGE_READ, Date(), user1, channel1.cid, channel1.type, channel1.id, null)

    val notificationMutesUpdated = NotificationMutesUpdatedEvent(EventType.NOTIFICATION_MUTES_UPDATED, Date(), me1)

    val user1Banned = ChannelUserBannedEvent(EventType.USER_BANNED, Date(), channel2.cid, channel2.type, channel2.id, user1, null)
    val user1Unbanned = ChannelUserUnbannedEvent(EventType.USER_UNBANNED, Date(), user1, channel2.cid, channel2.type, channel2.id)

    val user1ReadNotification = NotificationMarkReadEvent(EventType.NOTIFICATION_MARK_READ, Date(), user1, channel2.cid, channel2.type, channel2.id, null, null, null)
    val user1Read = MessageReadEvent(EventType.MESSAGE_READ, Date(), user1, channel2.cid, channel2.type, channel2.id, null)
    val memberAddedToChannelEvent = MemberAddedEvent(EventType.MEMBER_ADDED, Date(), user1, channel1WithNewMember.cid, channel1WithNewMember.type, channel1WithNewMember.id, member2)

    // member removed doesn't have a cid
    val memberRemovedFromChannel = MemberRemovedEvent(EventType.MEMBER_REMOVED, Date(), member2.user, channel1.cid, channel1.type, channel1.id)

    val notificationRemovedFromChannel = NotificationRemovedFromChannelEvent(EventType.NOTIFICATION_REMOVED_FROM_CHANNEL, Date(), user1, channel1.cid, channel1.type, channel1.id)

    // for whatever reason these events don't have event.cid
    val notificationAddedToChannelEvent = NotificationAddedToChannelEvent(EventType.NOTIFICATION_ADDED_TO_CHANNEL, Date(), channel1.cid, channel1.type, channel1.id, channel1)
    val notificationAddedToChannel2Event = NotificationAddedToChannelEvent(EventType.NOTIFICATION_ADDED_TO_CHANNEL, Date(), channel2.cid, channel2.type, channel2.id, channel2)
    // no created by
    val notificationAddedToChannel3Event = NotificationAddedToChannelEvent(EventType.NOTIFICATION_ADDED_TO_CHANNEL, Date(), channel3.cid, channel3.type, channel3.id, channel3)
    val user1UpdatedEvent = UserUpdatedEvent(EventType.USER_UPDATED, Date(), user1updated)
    val syncHistoryResult: Result<List<ChatEvent>> = Result(listOf(notificationAddedToChannelEvent, newMessageEvent, newMessageEvent2))

    val channelTruncatedEvent = ChannelTruncatedEvent(EventType.CHANNEL_TRUNCATED, Date(), channel1.cid, channel1.type, channel1.id, user1, channel1)
    val notificationChannelTruncated = NotificationChannelTruncatedEvent(EventType.NOTIFICATION_CHANNEL_TRUNCATED, Date(), channel1.cid, channel1.type, channel1.id, user1, channel1)
    val channelDeletedEvent = ChannelDeletedEvent(EventType.CHANNEL_DELETED, Date(), channel1.cid, channel1.type, channel1.id, channel1, null)

    val syncState = SyncStateEntity(user1.id, lastSyncedAt = Date.from(Instant.now()))
}

internal fun calendar(
    year: Int,
    month: Int,
    date: Int,
    hourOfDay: Int = 0,
    minute: Int = 0,
    seconds: Int = 0
): Date {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, date, hourOfDay, minute, seconds)
    return calendar.time
}
