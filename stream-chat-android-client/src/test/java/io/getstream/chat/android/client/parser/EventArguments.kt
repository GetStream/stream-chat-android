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

package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.createChannelDeletedEventStringJson
import io.getstream.chat.android.client.createChannelHiddenEventStringJson
import io.getstream.chat.android.client.createChannelTruncatedEventStringJson
import io.getstream.chat.android.client.createChannelTruncatedServerSideEventStringJson
import io.getstream.chat.android.client.createChannelUpdatedByUserEventStringJson
import io.getstream.chat.android.client.createChannelUpdatedEventStringJson
import io.getstream.chat.android.client.createChannelUserBannedEventStringJson
import io.getstream.chat.android.client.createChannelUserUnbannedEventStringJson
import io.getstream.chat.android.client.createChannelVisibleEventStringJson
import io.getstream.chat.android.client.createConnectedEventStringJson
import io.getstream.chat.android.client.createGlobalUserBannedEventStringJson
import io.getstream.chat.android.client.createGlobalUserUnbannedEventStringJson
import io.getstream.chat.android.client.createHealthEventStringJson
import io.getstream.chat.android.client.createMarkAllReadEventStringJson
import io.getstream.chat.android.client.createMemberAddedEventStringJson
import io.getstream.chat.android.client.createMemberRemovedEventStringJson
import io.getstream.chat.android.client.createMemberUpdatedEventStringJson
import io.getstream.chat.android.client.createMessageDeletedEventStringJson
import io.getstream.chat.android.client.createMessageDeletedServerSideEventStringJson
import io.getstream.chat.android.client.createMessageReadEventStringJson
import io.getstream.chat.android.client.createMessageUpdatedEventStringJson
import io.getstream.chat.android.client.createNewMessageEventStringJson
import io.getstream.chat.android.client.createNewMessageWithoutUnreadCountsEventStringJson
import io.getstream.chat.android.client.createNotificationAddedToChannelEventStringJson
import io.getstream.chat.android.client.createNotificationChannelDeletedEventStringJson
import io.getstream.chat.android.client.createNotificationChannelMutesUpdatedEventStringJson
import io.getstream.chat.android.client.createNotificationChannelTruncatedEventStringJson
import io.getstream.chat.android.client.createNotificationInviteAcceptedEventStringJson
import io.getstream.chat.android.client.createNotificationInviteRejectedEventStringJson
import io.getstream.chat.android.client.createNotificationInvitedEventStringJson
import io.getstream.chat.android.client.createNotificationMarkReadEventStringJson
import io.getstream.chat.android.client.createNotificationMarkUnreadEventStringJson
import io.getstream.chat.android.client.createNotificationMessageNewEventStringJson
import io.getstream.chat.android.client.createNotificationMutesUpdatedEventStringJson
import io.getstream.chat.android.client.createNotificationRemovedFromChannelEventStringJson
import io.getstream.chat.android.client.createReactionDeletedEventStringJson
import io.getstream.chat.android.client.createReactionNewEventStringJson
import io.getstream.chat.android.client.createReactionUpdateEventStringJson
import io.getstream.chat.android.client.createTypingStartEventStringJson
import io.getstream.chat.android.client.createTypingStopEventStringJson
import io.getstream.chat.android.client.createUnknownEventStringJson
import io.getstream.chat.android.client.createUserDeletedEventStringJson
import io.getstream.chat.android.client.createUserPresenceChangedEventStringJson
import io.getstream.chat.android.client.createUserStartWatchingEventStringJson
import io.getstream.chat.android.client.createUserStopWatchingEventStringJson
import io.getstream.chat.android.client.createUserUpdatedEventStringJson
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.MemberUpdatedEvent
import io.getstream.chat.android.client.events.MessageDeletedEvent
import io.getstream.chat.android.client.events.MessageReadEvent
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.events.NotificationInviteAcceptedEvent
import io.getstream.chat.android.client.events.NotificationInviteRejectedEvent
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
import io.getstream.chat.android.client.events.NotificationMarkUnreadEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationMutesUpdatedEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.events.ReactionDeletedEvent
import io.getstream.chat.android.client.events.ReactionNewEvent
import io.getstream.chat.android.client.events.ReactionUpdateEvent
import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.events.TypingStopEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.events.UserDeletedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.parser2.adapters.internal.StreamDateFormatter
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelInfo
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Config
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import org.junit.jupiter.params.provider.Arguments
import java.util.Date

private val streamDateFormatter = StreamDateFormatter()

@Suppress("LargeClass")
internal object EventArguments {
    private val date = Date(1593411268000)
    private const val dateString = "2020-06-29T06:14:28.000Z"
    private const val connectionId = "6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
    private const val channelType = "channelType"
    private const val channelId = "channelId"
    private const val cid = "channelType:channelId"
    private const val parentMessageId = "parentMessageId"
    private const val watcherCount = 3
    private const val unreadChannels = 5
    private const val unreadMessages = 1
    private const val totalUnreadCount = 4
    private val user = User(
        id = "bender",
        role = "user",
        invisible = false,
        banned = false,
        online = true,
        createdAt = date,
        updatedAt = date,
        lastActive = date,
        unreadChannels = 2,
        totalUnreadCount = 26,
        name = "Bender",
        image = "https://api.adorable.io/avatars/285/bender.png",
        extraData = mutableMapOf(),
    )

    private val member = Member(
        user,
        channelRole = "channel_member",
        createdAt = date,
        updatedAt = date,
    )

    private val giphyCommand = Command(
        name = "giphy",
        description = "Post a random gif to the channel",
        args = "[text]",
        set = "fun_set",
    )

    private val config = Config(
        createdAt = date,
        updatedAt = date,
        name = "team",
        typingEventsEnabled = true,
        readEventsEnabled = true,
        connectEventsEnabled = true,
        searchEnabled = true,
        isReactionsEnabled = true,
        isThreadEnabled = true,
        muteEnabled = true,
        uploadsEnabled = true,
        urlEnrichmentEnabled = true,
        customEventsEnabled = true,
        pushNotificationsEnabled = true,
        messageRetention = "infinite",
        maxMessageLength = 5000,
        automod = "disabled",
        automodBehavior = "flag",
        blocklistBehavior = "flag",
        commands = mutableListOf(giphyCommand),
    )

    private val channel = Channel(
        id = channelId,
        type = channelType,
        createdAt = date,
        updatedAt = date,
        createdBy = user,
        frozen = false,
        members = listOf(member),
        memberCount = 1,
        config = config,
    )

    private val channelInfo = ChannelInfo(
        cid = channel.cid,
        memberCount = channel.memberCount,
        id = channel.id,
        type = channel.type,
        name = "Channel Name",
        image = "https://domain.io/avatars/285/channel.png",
    )

    private val message = Message(
        id = "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
        text = "Hello",
        html = "<p>Hello</p>",
        user = user,
        createdAt = date,
        updatedAt = date,
        cid = channel.cid,
        type = "regular",
        channelInfo = channelInfo,
    )

    private val reaction = Reaction(
        messageId = "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
        type = "type",
        score = 3,
        user = user,
        userId = "bender",
        createdAt = date,
    )

    private val channelDeletedEvent = ChannelDeletedEvent(
        type = EventType.CHANNEL_DELETED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        user = user,
    )

    private val channelHiddenEvent = ChannelHiddenEvent(
        type = EventType.CHANNEL_HIDDEN,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user,
        channel = channel,
        clearHistory = true,
    )

    private val channelTruncatedEvent = ChannelTruncatedEvent(
        type = EventType.CHANNEL_TRUNCATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        user = user,
        message = null,
    )
    private val channelTruncatedServerSideEvent = ChannelTruncatedEvent(
        type = EventType.CHANNEL_TRUNCATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        user = null,
        message = null,
    )
    private val channelUpdatedEvent = ChannelUpdatedEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        channel = channel,
    )
    private val channelUpdatedByUserEvent = ChannelUpdatedByUserEvent(
        type = EventType.CHANNEL_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user,
        channel = channel,
        message = message,
    )
    private val channelVisibleEvent = ChannelVisibleEvent(
        type = EventType.CHANNEL_VISIBLE,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        user = user,
    )
    private val memberAddedEvent = MemberAddedEvent(
        type = EventType.MEMBER_ADDED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member,
    )
    private val memberRemovedEvent = MemberRemovedEvent(
        type = EventType.MEMBER_REMOVED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member,
    )
    private val memberUpdatedEvent = MemberUpdatedEvent(
        type = EventType.MEMBER_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        member = member,
    )
    private val messageDeletedEvent = MessageDeletedEvent(
        type = EventType.MESSAGE_DELETED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        hardDelete = false,
        channelMessageCount = 1,
        deletedForMe = false,
    )
    private val messageDeletedServerSideEvent = MessageDeletedEvent(
        type = EventType.MESSAGE_DELETED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = null,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        hardDelete = true,
        channelMessageCount = 1,
        deletedForMe = true,
    )
    private val messageReadEvent = MessageReadEvent(
        type = EventType.MESSAGE_READ,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        lastReadMessageId = message.id,
        team = null,
    )
    private val messageUpdatedEvent = MessageUpdatedEvent(
        type = EventType.MESSAGE_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
    )
    private val notificationAddedToChannelEvent = NotificationAddedToChannelEvent(
        type = EventType.NOTIFICATION_ADDED_TO_CHANNEL,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        member = member,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
    private val notificationChannelDeletedEvent = NotificationChannelDeletedEvent(
        type = EventType.NOTIFICATION_CHANNEL_DELETED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
    )
    private val notificationChannelTruncatedEvent = NotificationChannelTruncatedEvent(
        type = EventType.NOTIFICATION_CHANNEL_TRUNCATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
    )
    private val notificationInviteAcceptedEvent = NotificationInviteAcceptedEvent(
        type = EventType.NOTIFICATION_INVITE_ACCEPTED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user,
        member = member,
        channel = channel,
    )
    private val notificationInviteRejectedEvent = NotificationInviteRejectedEvent(
        type = EventType.NOTIFICATION_INVITE_REJECTED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user,
        member = member,
        channel = channel,
    )
    private val notificationInvitedEvent = NotificationInvitedEvent(
        type = EventType.NOTIFICATION_INVITED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user,
        member = member,
    )
    private val notificationMarkReadEvent = NotificationMarkReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
        lastReadMessageId = message.id,
    )
    private val notificationMarkUnreadEvent = NotificationMarkUnreadEvent(
        type = EventType.NOTIFICATION_MARK_UNREAD,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
        unreadMessages = unreadMessages,
        firstUnreadMessageId = message.id,
        lastReadMessageAt = date,
        lastReadMessageId = parentMessageId,
    )
    private val notificationMessageNewEvent = NotificationMessageNewEvent(
        type = EventType.NOTIFICATION_MESSAGE_NEW,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        message = message,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
    )
    private val notificationRemovedFromChannelEvent = NotificationRemovedFromChannelEvent(
        type = EventType.NOTIFICATION_REMOVED_FROM_CHANNEL,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        channel = channel,
        member = member,
    )
    private val reactionDeletedEvent = ReactionDeletedEvent(
        type = EventType.REACTION_DELETED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        reaction = reaction,
    )
    private val reactionNewEvent = ReactionNewEvent(
        type = EventType.REACTION_NEW,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        reaction = reaction,
    )
    private val reactionUpdateEvent = ReactionUpdateEvent(
        type = EventType.REACTION_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        reaction = reaction,
    )
    private val typingStartEvent = TypingStartEvent(
        type = EventType.TYPING_START,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentMessageId,
    )
    private val typingStopEvent = TypingStopEvent(
        type = EventType.TYPING_STOP,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        parentId = parentMessageId,
    )
    private val channelUserBannedEvent = ChannelUserBannedEvent(
        type = EventType.USER_BANNED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        user = user,
        expiration = date,
        shadow = false,
    )
    private val globalUserBannedEvent = GlobalUserBannedEvent(
        type = EventType.USER_BANNED,
        user = user,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
    )
    private val userDeletedEvent = UserDeletedEvent(
        type = EventType.USER_DELETED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
    )
    private val userPresenceChangedEvent = UserPresenceChangedEvent(
        type = EventType.USER_PRESENCE_CHANGED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
    )
    private val userStartWatchingEvent = UserStartWatchingEvent(
        type = EventType.USER_WATCHING_START,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        watcherCount = watcherCount,
        channelType = channelType,
        channelId = channelId,
        user = user,
    )
    private val userStopWatchingEvent = UserStopWatchingEvent(
        type = EventType.USER_WATCHING_STOP,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        cid = cid,
        watcherCount = watcherCount,
        channelType = channelType,
        channelId = channelId,
        user = user,
    )
    private val channelUserUnbannedEvent = ChannelUserUnbannedEvent(
        type = EventType.USER_UNBANNED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
    )
    private val globalUserUnbannedEvent = GlobalUserUnbannedEvent(
        type = EventType.USER_UNBANNED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
    )
    private val userUpdatedEvent = UserUpdatedEvent(
        type = EventType.USER_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
    )
    private val healthEvent = HealthEvent(
        type = EventType.HEALTH_CHECK,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        connectionId = connectionId,
    )
    private val connectedEvent = ConnectedEvent(
        type = EventType.HEALTH_CHECK,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        me = user,
        connectionId = connectionId,
    )
    private val notificationChannelMutesUpdatedEvent = NotificationChannelMutesUpdatedEvent(
        type = EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        me = user,
    )
    private val notificationMutesUpdatedEvent = NotificationMutesUpdatedEvent(
        type = EventType.NOTIFICATION_MUTES_UPDATED,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        me = user,
    )
    private val newMessageEvent = NewMessageEvent(
        type = EventType.MESSAGE_NEW,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        watcherCount = watcherCount,
        totalUnreadCount = totalUnreadCount,
        unreadChannels = unreadChannels,
        channelMessageCount = 1,
    )
    private val newMessageWithoutUnreadCountsEvent = NewMessageEvent(
        type = EventType.MESSAGE_NEW,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
        cid = cid,
        channelType = channelType,
        channelId = channelId,
        message = message,
        watcherCount = watcherCount,
        channelMessageCount = 1,
    )
    private val unknownEvent = UnknownEvent(
        type = EventType.UNKNOWN,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = null,
        rawData = mapOf("type" to EventType.UNKNOWN, "created_at" to dateString),
    )
    private val otherUnknownEvent = UnknownEvent(
        type = "some.unknown.type",
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = null,
        rawData = mapOf("type" to "some.unknown.type", "created_at" to dateString),
    )
    private val markAllReadEvent = MarkAllReadEvent(
        type = EventType.NOTIFICATION_MARK_READ,
        createdAt = date,
        rawCreatedAt = streamDateFormatter.format(date),
        user = user,
    )

    private val events: List<ChatEvent> = listOf(
        channelTruncatedEvent,
        channelTruncatedServerSideEvent,
        channelUpdatedEvent,
        channelUpdatedByUserEvent,
        channelVisibleEvent,
        memberAddedEvent,
        memberRemovedEvent,
        memberUpdatedEvent,
        messageDeletedEvent,
        messageDeletedServerSideEvent,
        messageReadEvent,
        messageUpdatedEvent,
        notificationAddedToChannelEvent,
        notificationChannelDeletedEvent,
        notificationChannelTruncatedEvent,
        notificationInviteAcceptedEvent,
        notificationInviteRejectedEvent,
        notificationInvitedEvent,
        notificationMarkReadEvent,
        notificationMarkUnreadEvent,
        notificationMessageNewEvent,
        notificationRemovedFromChannelEvent,
        reactionDeletedEvent,
        reactionNewEvent,
        reactionUpdateEvent,
        typingStartEvent,
        typingStopEvent,
        channelUserBannedEvent,
        globalUserBannedEvent,
        userDeletedEvent,
        userPresenceChangedEvent,
        userStartWatchingEvent,
        userStopWatchingEvent,
        channelUserUnbannedEvent,
        globalUserUnbannedEvent,
        userUpdatedEvent,
        channelDeletedEvent,
        channelHiddenEvent,
        notificationChannelMutesUpdatedEvent,
        notificationMutesUpdatedEvent,
        newMessageEvent,
        newMessageWithoutUnreadCountsEvent,
        markAllReadEvent,
    )

    private fun eventArgumentsList() = listOf(
        Arguments.of(createChannelTruncatedEventStringJson(), channelTruncatedEvent),
        Arguments.of(createChannelTruncatedServerSideEventStringJson(), channelTruncatedServerSideEvent),
        Arguments.of(createChannelUpdatedEventStringJson(), channelUpdatedEvent),
        Arguments.of(createChannelUpdatedByUserEventStringJson(), channelUpdatedByUserEvent),
        Arguments.of(createChannelVisibleEventStringJson(), channelVisibleEvent),
        Arguments.of(createMemberAddedEventStringJson(), memberAddedEvent),
        Arguments.of(createMemberRemovedEventStringJson(), memberRemovedEvent),
        Arguments.of(createMemberUpdatedEventStringJson(), memberUpdatedEvent),
        Arguments.of(createMessageDeletedEventStringJson(), messageDeletedEvent),
        Arguments.of(createMessageDeletedServerSideEventStringJson(), messageDeletedServerSideEvent),
        Arguments.of(createMessageReadEventStringJson(), messageReadEvent),
        Arguments.of(createMessageUpdatedEventStringJson(), messageUpdatedEvent),
        Arguments.of(createNotificationAddedToChannelEventStringJson(), notificationAddedToChannelEvent),
        Arguments.of(createNotificationChannelDeletedEventStringJson(), notificationChannelDeletedEvent),
        Arguments.of(createNotificationChannelTruncatedEventStringJson(), notificationChannelTruncatedEvent),
        Arguments.of(createNotificationInviteAcceptedEventStringJson(), notificationInviteAcceptedEvent),
        Arguments.of(createNotificationInviteRejectedEventStringJson(), notificationInviteRejectedEvent),
        Arguments.of(createNotificationInvitedEventStringJson(), notificationInvitedEvent),
        Arguments.of(createNotificationMarkReadEventStringJson(), notificationMarkReadEvent),
        Arguments.of(createNotificationMarkUnreadEventStringJson(), notificationMarkUnreadEvent),
        Arguments.of(createNotificationMessageNewEventStringJson(), notificationMessageNewEvent),
        Arguments.of(createNotificationRemovedFromChannelEventStringJson(), notificationRemovedFromChannelEvent),
        Arguments.of(createReactionDeletedEventStringJson(), reactionDeletedEvent),
        Arguments.of(createReactionNewEventStringJson(), reactionNewEvent),
        Arguments.of(createReactionUpdateEventStringJson(), reactionUpdateEvent),
        Arguments.of(createTypingStartEventStringJson(), typingStartEvent),
        Arguments.of(createTypingStopEventStringJson(), typingStopEvent),
        Arguments.of(createChannelUserBannedEventStringJson(), channelUserBannedEvent),
        Arguments.of(createGlobalUserBannedEventStringJson(), globalUserBannedEvent),
        Arguments.of(createUserDeletedEventStringJson(), userDeletedEvent),
        Arguments.of(createUserPresenceChangedEventStringJson(), userPresenceChangedEvent),
        Arguments.of(createUserStartWatchingEventStringJson(), userStartWatchingEvent),
        Arguments.of(createUserStopWatchingEventStringJson(), userStopWatchingEvent),
        Arguments.of(createChannelUserUnbannedEventStringJson(), channelUserUnbannedEvent),
        Arguments.of(createGlobalUserUnbannedEventStringJson(), globalUserUnbannedEvent),
        Arguments.of(createUserUpdatedEventStringJson(), userUpdatedEvent),
        Arguments.of(createConnectedEventStringJson(), connectedEvent),
        Arguments.of(createConnectedEventStringJson(null), healthEvent),
        Arguments.of(createChannelDeletedEventStringJson(), channelDeletedEvent),
        Arguments.of(createChannelHiddenEventStringJson(), channelHiddenEvent),
        Arguments.of(createHealthEventStringJson(), healthEvent),
        Arguments.of(createNotificationChannelMutesUpdatedEventStringJson(), notificationChannelMutesUpdatedEvent),
        Arguments.of(createNotificationMutesUpdatedEventStringJson(), notificationMutesUpdatedEvent),
        Arguments.of(createNewMessageEventStringJson(), newMessageEvent),
        Arguments.of(createNewMessageWithoutUnreadCountsEventStringJson(), newMessageWithoutUnreadCountsEvent),
        Arguments.of(createUnknownEventStringJson(), unknownEvent),
        Arguments.of(createUnknownEventStringJson("some.unknown.type"), otherUnknownEvent),
        Arguments.of(createMarkAllReadEventStringJson(), markAllReadEvent),
    )

    fun randomEvent(): ChatEvent = events.random()

    @JvmStatic
    fun eventAdapterArgumentsList() = eventArgumentsList()

    @JvmStatic
    fun chatParserEventArgumentsList() = eventArgumentsList()
}
