package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.createChannelCreatedEventStringJson
import io.getstream.chat.android.client.createChannelDeletedEventStringJson
import io.getstream.chat.android.client.createChannelHiddenEventStringJson
import io.getstream.chat.android.client.createChannelMuteEventStringJson
import io.getstream.chat.android.client.createChannelTruncatedEventStringJson
import io.getstream.chat.android.client.createChannelUnmuteEventStringJson
import io.getstream.chat.android.client.createChannelUpdatedByUserEventStringJson
import io.getstream.chat.android.client.createChannelUpdatedEventStringJson
import io.getstream.chat.android.client.createChannelUserBannedEventStringJson
import io.getstream.chat.android.client.createChannelUserUnbannedEventStringJson
import io.getstream.chat.android.client.createChannelVisibleEventStringJson
import io.getstream.chat.android.client.createChannelsMuteEventStringJson
import io.getstream.chat.android.client.createChannelsUnmuteEventStringJson
import io.getstream.chat.android.client.createConnectedEventStringJson
import io.getstream.chat.android.client.createGlobalUserBannedEventStringJson
import io.getstream.chat.android.client.createGlobalUserUnbannedEventStringJson
import io.getstream.chat.android.client.createHealthEventStringJson
import io.getstream.chat.android.client.createMarkAllReadEventStringJson
import io.getstream.chat.android.client.createMemberAddedEventStringJson
import io.getstream.chat.android.client.createMemberRemovedEventStringJson
import io.getstream.chat.android.client.createMemberUpdatedEventStringJson
import io.getstream.chat.android.client.createMessageDeletedEventStringJson
import io.getstream.chat.android.client.createMessageReadEventStringJson
import io.getstream.chat.android.client.createMessageUpdatedEventStringJson
import io.getstream.chat.android.client.createNewMessageEventStringJson
import io.getstream.chat.android.client.createNotificationAddedToChannelEventStringJson
import io.getstream.chat.android.client.createNotificationChannelDeletedEventStringJson
import io.getstream.chat.android.client.createNotificationChannelMutesUpdatedEventStringJson
import io.getstream.chat.android.client.createNotificationChannelTruncatedEventStringJson
import io.getstream.chat.android.client.createNotificationInviteAcceptedEventStringJson
import io.getstream.chat.android.client.createNotificationInvitedEventStringJson
import io.getstream.chat.android.client.createNotificationMarkReadEventStringJson
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
import io.getstream.chat.android.client.createUserMutedEventStringJson
import io.getstream.chat.android.client.createUserPresenceChangedEventStringJson
import io.getstream.chat.android.client.createUserStartWatchingEventStringJson
import io.getstream.chat.android.client.createUserStopWatchingEventStringJson
import io.getstream.chat.android.client.createUserUnmutedEventStringJson
import io.getstream.chat.android.client.createUserUpdatedEventStringJson
import io.getstream.chat.android.client.createUsersMutedEventStringJson
import io.getstream.chat.android.client.createUsersUnmutedEventStringJson
import io.getstream.chat.android.client.events.ChannelCreatedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelMuteEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUnmuteEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.events.ChannelsUnmuteEvent
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
import io.getstream.chat.android.client.events.NotificationInvitedEvent
import io.getstream.chat.android.client.events.NotificationMarkReadEvent
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
import io.getstream.chat.android.client.events.UserMutedEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.events.UserUnmutedEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.client.events.UsersMutedEvent
import io.getstream.chat.android.client.events.UsersUnmutedEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelMute
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import org.junit.jupiter.params.provider.Arguments
import java.util.Date

@Suppress("unused") // used via reflection
internal object EventArguments {
    private val date = Date(1593411268000)
    private val dateString = "2020-06-29T06:14:28.000Z"
    private val connectionId = "6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
    private val channelType = "channelType"
    private val channelId = "channelId"
    private val cid = "channelType:channelId"
    private val parentMessageId = "parentMessageId"
    private val watcherCount = 3
    private val unreadChannels = 5
    private val totalUnreadCount = 4
    private val user = User(
        id = "bender",
        role = "user",
        online = true,
        createdAt = date,
        updatedAt = date,
        lastActive = date,
        unreadCount = 26,
        unreadChannels = 2,
        totalUnreadCount = 26,
        extraData = mutableMapOf("name" to "Bender", "image" to "https://api.adorable.io/avatars/285/bender.png")
    )

    private val member = Member(user, role = "user", createdAt = date, updatedAt = date)
    private val giphyCommand = Command("giphy", "Post a random gif to the channel", "[text]", "fun_set")
    private val config = Config(
        created_at = date,
        updated_at = date,
        name = "team",
        isTypingEvents = true,
        isReadEvents = true,
        isConnectEvents = true,
        isSearch = true,
        isReactionsEnabled = true,
        isRepliesEnabled = true,
        isMutes = true,
        maxMessageLength = 5000,
        automod = "disabled",
        commands = mutableListOf(giphyCommand)
    )
    private val channel = Channel(
        id = channelId,
        type = channelType,
        cid = cid,
        lastMessageAt = date,
        createdAt = date,
        updatedAt = date,
        createdBy = user,
        frozen = false,
        members = listOf(member),
        memberCount = 1,
        config = config
    )
    private val message = Message(
        id = "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
        text = "Hello",
        user = user,
        createdAt = date,
        updatedAt = date,
        cid = channel.cid
    )
    private val reaction = Reaction(
        messageId = "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
        type = "type",
        score = 3,
        user = user,
        userId = "bender",
        createdAt = date
    )
    private val channelMute = ChannelMute(user, channel, date)
    private val channelCreatedEvent = ChannelCreatedEvent(EventType.CHANNEL_CREATED, date, cid, channelType, channelId, user, message, channel)
    private val channelDeletedEvent = ChannelDeletedEvent(EventType.CHANNEL_DELETED, date, cid, channelType, channelId, channel, user)
    private val channelHiddenEvent = ChannelHiddenEvent(EventType.CHANNEL_HIDDEN, date, cid, channelType, channelId, user, clearHistory = true)
    private val channelMuteEvent = ChannelMuteEvent(EventType.CHANNEL_MUTED, date, channelMute)
    private val channelsMuteEvent = ChannelsMuteEvent(EventType.CHANNEL_MUTED, date, listOf(channelMute))
    private val channelTruncatedEvent = ChannelTruncatedEvent(EventType.CHANNEL_TRUNCATED, date, cid, channelType, channelId, user, channel)
    private val channelUnmuteEvent = ChannelUnmuteEvent(EventType.CHANNEL_UNMUTED, date, channelMute)
    private val channelsUnmuteEvent = ChannelsUnmuteEvent(EventType.CHANNEL_UNMUTED, date, listOf(channelMute))
    private val channelUpdatedEvent = ChannelUpdatedEvent(EventType.CHANNEL_UPDATED, date, cid, channelType, channelId, message, channel)
    private val channelUpdatedByUserEvent = ChannelUpdatedByUserEvent(EventType.CHANNEL_UPDATED, date, cid, channelType, channelId, user, message, channel)
    private val channelVisibleEvent = ChannelVisibleEvent(EventType.CHANNEL_VISIBLE, date, cid, channelType, channelId, user)
    private val memberAddedEvent = MemberAddedEvent(EventType.MEMBER_ADDED, date, user, cid, channelType, channelId, member)
    private val memberRemovedEvent = MemberRemovedEvent(EventType.MEMBER_REMOVED, date, user, cid, channelType, channelId)
    private val memberUpdatedEvent = MemberUpdatedEvent(EventType.MEMBER_UPDATED, date, user, cid, channelType, channelId, member)
    private val messageDeletedEvent = MessageDeletedEvent(EventType.MESSAGE_DELETED, date, user, cid, channelType, channelId, message, watcherCount)
    private val messageReadEvent = MessageReadEvent(EventType.MESSAGE_READ, date, user, cid, channelType, channelId, watcherCount)
    private val messageUpdatedEvent = MessageUpdatedEvent(EventType.MESSAGE_UPDATED, date, user, cid, channelType, channelId, message, watcherCount)
    private val notificationAddedToChannelEvent = NotificationAddedToChannelEvent(EventType.NOTIFICATION_ADDED_TO_CHANNEL, date, cid, channelType, channelId, channel)
    private val notificationChannelDeletedEvent = NotificationChannelDeletedEvent(EventType.NOTIFICATION_CHANNEL_DELETED, date, cid, channelType, channelId, channel, user)
    private val notificationChannelTruncatedEvent = NotificationChannelTruncatedEvent(EventType.NOTIFICATION_CHANNEL_TRUNCATED, date, cid, channelType, channelId, user, channel)
    private val notificationInviteAcceptedEvent = NotificationInviteAcceptedEvent(EventType.NOTIFICATION_INVITE_ACCEPTED, date, cid, channelType, channelId, user, member)
    private val notificationInvitedEvent = NotificationInvitedEvent(EventType.NOTIFICATION_INVITED, date, cid, channelType, channelId, user, member)
    private val notificationMarkReadEvent = NotificationMarkReadEvent(EventType.NOTIFICATION_MARK_READ, date, user, cid, channelType, channelId, watcherCount, totalUnreadCount, unreadChannels)
    private val notificationMessageNewEvent = NotificationMessageNewEvent(EventType.NOTIFICATION_MESSAGE_NEW, date, cid, channelType, channelId, channel, message, watcherCount, totalUnreadCount, unreadChannels)
    private val notificationRemovedFromChannelEvent = NotificationRemovedFromChannelEvent(EventType.NOTIFICATION_REMOVED_FROM_CHANNEL, date, user, cid, channelType, channelId)
    private val reactionDeletedEvent = ReactionDeletedEvent(EventType.REACTION_DELETED, date, user, cid, channelType, channelId, message, reaction)
    private val reactionNewEvent = ReactionNewEvent(EventType.REACTION_NEW, date, user, cid, channelType, channelId, message, reaction)
    private val reactionUpdateEvent = ReactionUpdateEvent(EventType.REACTION_UPDATED, date, user, cid, channelType, channelId, message, reaction)
    private val typingStartEvent = TypingStartEvent(EventType.TYPING_START, date, user, cid, channelType, channelId, parentMessageId)
    private val typingStopEvent = TypingStopEvent(EventType.TYPING_STOP, date, user, cid, channelType, channelId, parentMessageId)
    private val channelUserBannedEvent = ChannelUserBannedEvent(EventType.USER_BANNED, date, cid, channelType, channelId, user, date)
    private val globalUserBannedEvent = GlobalUserBannedEvent(EventType.USER_BANNED, user, date)
    private val userDeletedEvent = UserDeletedEvent(EventType.USER_DELETED, date, user)
    private val userMutedEvent = UserMutedEvent(EventType.USER_MUTED, date, user, user)
    private val usersMutedEvent = UsersMutedEvent(EventType.USER_MUTED, date, user, listOf(user))
    private val userPresenceChangedEvent = UserPresenceChangedEvent(EventType.USER_PRESENCE_CHANGED, date, user)
    private val userStartWatchingEvent = UserStartWatchingEvent(EventType.USER_WATCHING_START, date, cid, watcherCount, channelType, channelId, user)
    private val userStopWatchingEvent = UserStopWatchingEvent(EventType.USER_WATCHING_STOP, date, cid, watcherCount, channelType, channelId, user)
    private val channelUserUnbannedEvent = ChannelUserUnbannedEvent(EventType.USER_UNBANNED, date, user, cid, channelType, channelId)
    private val globalUserUnbannedEvent = GlobalUserUnbannedEvent(EventType.USER_UNBANNED, date, user)
    private val userUnmutedEvent = UserUnmutedEvent(EventType.USER_UNMUTED, date, user, user)
    private val usersUnmutedEvent = UsersUnmutedEvent(EventType.USER_UNMUTED, date, user, listOf(user))
    private val userUpdatedEvent = UserUpdatedEvent(EventType.USER_UPDATED, date, user)
    private val healthEvent = HealthEvent(EventType.HEALTH_CHECK, date, connectionId)
    private val connectedEvent = ConnectedEvent(EventType.HEALTH_CHECK, date, user, connectionId)
    private val notificationChannelMutesUpdatedEvent = NotificationChannelMutesUpdatedEvent(EventType.NOTIFICATION_CHANNEL_MUTES_UPDATED, date, user)
    private val notificationMutesUpdatedEvent = NotificationMutesUpdatedEvent(EventType.NOTIFICATION_MUTES_UPDATED, date, user)
    private val newMessageEvent = NewMessageEvent(EventType.MESSAGE_NEW, date, user, cid, channelType, channelId, message, watcherCount, totalUnreadCount, unreadChannels)
    private val unknownEvent = UnknownEvent(EventType.UNKNOWN, date, mapOf("type" to EventType.UNKNOWN, "created_at" to dateString))
    private val otherUnknownEvent = UnknownEvent("some.unknown.type", date, mapOf("type" to "some.unknown.type", "created_at" to dateString))
    private val markAllReadEvent = MarkAllReadEvent(EventType.NOTIFICATION_MARK_READ, date, user)

    private fun eventArguments() = listOf(
        Arguments.of(createChannelTruncatedEventStringJson(), channelTruncatedEvent),
        Arguments.of(createChannelUnmuteEventStringJson(), channelUnmuteEvent),
        Arguments.of(createChannelsUnmuteEventStringJson(), channelsUnmuteEvent),
        Arguments.of(createChannelUpdatedEventStringJson(), channelUpdatedEvent),
        Arguments.of(createChannelUpdatedByUserEventStringJson(), channelUpdatedByUserEvent),
        Arguments.of(createChannelVisibleEventStringJson(), channelVisibleEvent),
        Arguments.of(createMemberAddedEventStringJson(), memberAddedEvent),
        Arguments.of(createMemberRemovedEventStringJson(), memberRemovedEvent),
        Arguments.of(createMemberUpdatedEventStringJson(), memberUpdatedEvent),
        Arguments.of(createMessageDeletedEventStringJson(), messageDeletedEvent),
        Arguments.of(createMessageReadEventStringJson(), messageReadEvent),
        Arguments.of(createMessageUpdatedEventStringJson(), messageUpdatedEvent),
        Arguments.of(createNotificationAddedToChannelEventStringJson(), notificationAddedToChannelEvent),
        Arguments.of(createNotificationChannelDeletedEventStringJson(), notificationChannelDeletedEvent),
        Arguments.of(createNotificationChannelTruncatedEventStringJson(), notificationChannelTruncatedEvent),
        Arguments.of(createNotificationInviteAcceptedEventStringJson(), notificationInviteAcceptedEvent),
        Arguments.of(createNotificationInvitedEventStringJson(), notificationInvitedEvent),
        Arguments.of(createNotificationMarkReadEventStringJson(), notificationMarkReadEvent),
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
        Arguments.of(createUserMutedEventStringJson(), userMutedEvent),
        Arguments.of(createUsersMutedEventStringJson(), usersMutedEvent),
        Arguments.of(createUserPresenceChangedEventStringJson(), userPresenceChangedEvent),
        Arguments.of(createUserStartWatchingEventStringJson(), userStartWatchingEvent),
        Arguments.of(createUserStopWatchingEventStringJson(), userStopWatchingEvent),
        Arguments.of(createChannelUserUnbannedEventStringJson(), channelUserUnbannedEvent),
        Arguments.of(createGlobalUserUnbannedEventStringJson(), globalUserUnbannedEvent),
        Arguments.of(createUserUnmutedEventStringJson(), userUnmutedEvent),
        Arguments.of(createUsersUnmutedEventStringJson(), usersUnmutedEvent),
        Arguments.of(createUserUpdatedEventStringJson(), userUpdatedEvent),
        Arguments.of(createConnectedEventStringJson(), connectedEvent),
        Arguments.of(createConnectedEventStringJson(null), healthEvent),
        Arguments.of(createChannelCreatedEventStringJson(), channelCreatedEvent),
        Arguments.of(createChannelDeletedEventStringJson(), channelDeletedEvent),
        Arguments.of(createChannelHiddenEventStringJson(), channelHiddenEvent),
        Arguments.of(createChannelMuteEventStringJson(), channelMuteEvent),
        Arguments.of(createChannelsMuteEventStringJson(), channelsMuteEvent),
        Arguments.of(createHealthEventStringJson(), healthEvent),
        Arguments.of(createNotificationChannelMutesUpdatedEventStringJson(), notificationChannelMutesUpdatedEvent),
        Arguments.of(createNotificationMutesUpdatedEventStringJson(), notificationMutesUpdatedEvent),
        Arguments.of(createNewMessageEventStringJson(), newMessageEvent),
        Arguments.of(createUnknownEventStringJson(), unknownEvent),
        Arguments.of(createUnknownEventStringJson("some.unknown.type"), otherUnknownEvent),
        Arguments.of(createMarkAllReadEventStringJson(), markAllReadEvent),
    )

    @JvmStatic
    fun eventAdapterArguments() = eventArguments()

    @JvmStatic
    fun chatParserEventArguments() = eventArguments()
}
