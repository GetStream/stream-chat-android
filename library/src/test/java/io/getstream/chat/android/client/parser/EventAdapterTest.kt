package io.getstream.chat.android.client.parser

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import io.getstream.chat.android.client.createChannelCreatedEventStringReader
import io.getstream.chat.android.client.createChannelDeletedEventStringReader
import io.getstream.chat.android.client.createChannelHiddenEventStringReader
import io.getstream.chat.android.client.createChannelMuteEventStringReader
import io.getstream.chat.android.client.createChannelTruncatedEventStringReader
import io.getstream.chat.android.client.createChannelUnmuteEventStringReader
import io.getstream.chat.android.client.createChannelUpdatedEventStringReader
import io.getstream.chat.android.client.createChannelUserBannedEventStringReader
import io.getstream.chat.android.client.createChannelUserUnbannedEventStringReader
import io.getstream.chat.android.client.createChannelVisibleEventStringReader
import io.getstream.chat.android.client.createChannelsMuteEventStringReader
import io.getstream.chat.android.client.createChannelsUnmuteEventStringReader
import io.getstream.chat.android.client.createConnectedEventStringReader
import io.getstream.chat.android.client.createGlobalUserBannedEventStringReader
import io.getstream.chat.android.client.createGlobalUserUnbannedEventStringReader
import io.getstream.chat.android.client.createHealthEventStringReader
import io.getstream.chat.android.client.createMemberAddedEventStringReader
import io.getstream.chat.android.client.createMemberRemovedEventStringReader
import io.getstream.chat.android.client.createMemberUpdatedEventStringReader
import io.getstream.chat.android.client.createMessageDeletedEventStringReader
import io.getstream.chat.android.client.createMessageReadEventStringReader
import io.getstream.chat.android.client.createMessageUpdatedEventStringReader
import io.getstream.chat.android.client.createNewMessageEventStringReader
import io.getstream.chat.android.client.createNotificationAddedToChannelEventStringReader
import io.getstream.chat.android.client.createNotificationChannelDeletedEventStringReader
import io.getstream.chat.android.client.createNotificationChannelMutesUpdatedEventStringReader
import io.getstream.chat.android.client.createNotificationChannelTruncatedEventStringReader
import io.getstream.chat.android.client.createNotificationInviteAcceptedEventStringReader
import io.getstream.chat.android.client.createNotificationInvitedEventStringReader
import io.getstream.chat.android.client.createNotificationMarkReadEventStringReader
import io.getstream.chat.android.client.createNotificationMessageNewEventStringReader
import io.getstream.chat.android.client.createNotificationMutesUpdatedEventStringReader
import io.getstream.chat.android.client.createNotificationRemovedFromChannelEventStringReader
import io.getstream.chat.android.client.createReactionDeletedEventStringReader
import io.getstream.chat.android.client.createReactionNewEventStringReader
import io.getstream.chat.android.client.createReactionUpdateEventStringReader
import io.getstream.chat.android.client.createTypingStartEventStringReader
import io.getstream.chat.android.client.createTypingStopEventStringReader
import io.getstream.chat.android.client.createUserDeletedEventStringReader
import io.getstream.chat.android.client.createUserMutedEventStringReader
import io.getstream.chat.android.client.createUserPresenceChangedEventStringReader
import io.getstream.chat.android.client.createUserStartWatchingEventStringReader
import io.getstream.chat.android.client.createUserStopWatchingEventStringReader
import io.getstream.chat.android.client.createUserUnmutedEventStringReader
import io.getstream.chat.android.client.createUserUpdatedEventStringReader
import io.getstream.chat.android.client.createUsersMutedEventStringReader
import io.getstream.chat.android.client.createUsersUnmutedEventStringReader
import io.getstream.chat.android.client.events.ChannelCreatedEvent
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelMuteEvent
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChannelUnmuteEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelUserBannedEvent
import io.getstream.chat.android.client.events.ChannelUserUnbannedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
import io.getstream.chat.android.client.events.ChannelsMuteEvent
import io.getstream.chat.android.client.events.ChannelsUnmuteEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.GlobalUserBannedEvent
import io.getstream.chat.android.client.events.GlobalUserUnbannedEvent
import io.getstream.chat.android.client.events.HealthEvent
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
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class EventAdapterTest {

    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(TypeAdapterFactory())
        .addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>): Boolean = false
            override fun shouldSkipField(f: FieldAttributes): Boolean =
                f.getAnnotation(IgnoreSerialisation::class.java) != null
        })
        .addDeserializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipClass(clazz: Class<*>): Boolean = false
            override fun shouldSkipField(f: FieldAttributes): Boolean =
                f.getAnnotation(IgnoreDeserialisation::class.java) != null
        })
        .create()
    private val evenAdapter = EventAdapter(gson, gson.getAdapter(ChatEvent::class.java))
    private val date = Date(1593411268000)
    private val connectionId = "6cfffec7-40df-40ac-901a-6ea6c5b7fb83"
    private val channelType = "channelType"
    private val channelId = "channelId"
    private val cid = "channelType:channelId"
    private val watcherCount = 3
    private val unreadMessages = 5
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
    private val message = Message(
        id = "09afcd85-9dbb-4da8-8d85-5a6b4268d755",
        text = "Hello",
        user = user,
        createdAt = date,
        updatedAt = date
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
    private val channelUpdatedEvent = ChannelUpdatedEvent(EventType.CHANNEL_UPDATED, date, cid, channelType, channelId, user, message, channel)
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
    private val notificationMarkReadEvent = NotificationMarkReadEvent(EventType.NOTIFICATION_MARK_READ, date, user, cid, channelType, channelId, watcherCount, unreadMessages, totalUnreadCount)
    private val notificationMessageNewEvent = NotificationMessageNewEvent(EventType.NOTIFICATION_MESSAGE_NEW, date, user, cid, channelType, channelId, message, watcherCount, unreadMessages, totalUnreadCount)
    private val notificationRemovedFromChannelEvent = NotificationRemovedFromChannelEvent(EventType.NOTIFICATION_REMOVED_FROM_CHANNEL, date, user, cid, channelType, channelId)
    private val reactionDeletedEvent = ReactionDeletedEvent(EventType.REACTION_DELETED, date, user, cid, channelType, channelId, message, reaction)
    private val reactionNewEvent = ReactionNewEvent(EventType.REACTION_NEW, date, user, cid, channelType, channelId, message, reaction)
    private val reactionUpdateEvent = ReactionUpdateEvent(EventType.REACTION_UPDATED, date, user, cid, channelType, channelId, message, reaction)
    private val typingStartEvent = TypingStartEvent(EventType.TYPING_START, date, user, cid, channelType, channelId)
    private val typingStopEvent = TypingStopEvent(EventType.TYPING_STOP, date, user, cid, channelType, channelId)
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
    private val newMessageEvent = NewMessageEvent(EventType.MESSAGE_NEW, date, user, cid, channelType, channelId, message, watcherCount, unreadMessages, totalUnreadCount)

    @Test
    fun `Should read ChannelTruncatedEvent`() {
        evenAdapter.read(JsonReader(createChannelTruncatedEventStringReader())) `should be equal to` channelTruncatedEvent
    }

    @Test
    fun `Should read ChannelUnmuteEvent`() {
        evenAdapter.read(JsonReader(createChannelUnmuteEventStringReader())) `should be equal to` channelUnmuteEvent
    }

    @Test
    fun `Should read ChannelsUnmuteEvent`() {
        evenAdapter.read(JsonReader(createChannelsUnmuteEventStringReader())) `should be equal to` channelsUnmuteEvent
    }

    @Test
    fun `Should read ChannelUpdatedEvent`() {
        evenAdapter.read(JsonReader(createChannelUpdatedEventStringReader())) `should be equal to` channelUpdatedEvent
    }

    @Test
    fun `Should read ChannelVisibleEvent`() {
        evenAdapter.read(JsonReader(createChannelVisibleEventStringReader())) `should be equal to` channelVisibleEvent
    }

    @Test
    fun `Should read MemberAddedEvent`() {
        evenAdapter.read(JsonReader(createMemberAddedEventStringReader())) `should be equal to` memberAddedEvent
    }

    @Test
    fun `Should read MemberRemovedEvent`() {
        evenAdapter.read(JsonReader(createMemberRemovedEventStringReader())) `should be equal to` memberRemovedEvent
    }

    @Test
    fun `Should read MemberUpdatedEvent`() {
        evenAdapter.read(JsonReader(createMemberUpdatedEventStringReader())) `should be equal to` memberUpdatedEvent
    }

    @Test
    fun `Should read MessageDeletedEvent`() {
        evenAdapter.read(JsonReader(createMessageDeletedEventStringReader())) `should be equal to` messageDeletedEvent
    }

    @Test
    fun `Should read MessageReadEvent`() {
        evenAdapter.read(JsonReader(createMessageReadEventStringReader())) `should be equal to` messageReadEvent
    }

    @Test
    fun `Should read MessageUpdatedEvent`() {
        evenAdapter.read(JsonReader(createMessageUpdatedEventStringReader())) `should be equal to` messageUpdatedEvent
    }

    @Test
    fun `Should read NotificationAddedToChannelEvent`() {
        evenAdapter.read(JsonReader(createNotificationAddedToChannelEventStringReader())) `should be equal to` notificationAddedToChannelEvent
    }

    @Test
    fun `Should read NotificationChannelDeletedEvent`() {
        evenAdapter.read(JsonReader(createNotificationChannelDeletedEventStringReader())) `should be equal to` notificationChannelDeletedEvent
    }

    @Test
    fun `Should read NotificationChannelTruncatedEvent`() {
        evenAdapter.read(JsonReader(createNotificationChannelTruncatedEventStringReader())) `should be equal to` notificationChannelTruncatedEvent
    }

    @Test
    fun `Should read NotificationInviteAcceptedEvent`() {
        evenAdapter.read(JsonReader(createNotificationInviteAcceptedEventStringReader())) `should be equal to` notificationInviteAcceptedEvent
    }

    @Test
    fun `Should read NotificationInvitedEvent`() {
        evenAdapter.read(JsonReader(createNotificationInvitedEventStringReader())) `should be equal to` notificationInvitedEvent
    }

    @Test
    fun `Should read NotificationMarkReadEvent`() {
        evenAdapter.read(JsonReader(createNotificationMarkReadEventStringReader())) `should be equal to` notificationMarkReadEvent
    }

    @Test
    fun `Should read NotificationMessageNewEvent`() {
        evenAdapter.read(JsonReader(createNotificationMessageNewEventStringReader())) `should be equal to` notificationMessageNewEvent
    }

    @Test
    fun `Should read NotificationRemovedFromChannelEvent`() {
        evenAdapter.read(JsonReader(createNotificationRemovedFromChannelEventStringReader())) `should be equal to` notificationRemovedFromChannelEvent
    }

    @Test
    fun `Should read ReactionDeletedEvent`() {
        evenAdapter.read(JsonReader(createReactionDeletedEventStringReader())) `should be equal to` reactionDeletedEvent
    }

    @Test
    fun `Should read ReactionNewEvent`() {
        evenAdapter.read(JsonReader(createReactionNewEventStringReader())) `should be equal to` reactionNewEvent
    }

    @Test
    fun `Should read ReactionUpdateEvent`() {
        evenAdapter.read(JsonReader(createReactionUpdateEventStringReader())) `should be equal to` reactionUpdateEvent
    }

    @Test
    fun `Should read TypingStartEvent`() {
        evenAdapter.read(JsonReader(createTypingStartEventStringReader())) `should be equal to` typingStartEvent
    }

    @Test
    fun `Should read TypingStopEvent`() {
        evenAdapter.read(JsonReader(createTypingStopEventStringReader())) `should be equal to` typingStopEvent
    }

    @Test
    fun `Should read ChannelUserBannedEvent`() {
        evenAdapter.read(JsonReader(createChannelUserBannedEventStringReader())) `should be equal to` channelUserBannedEvent
    }

    @Test
    fun `Should read GlobalUserBannedEvent`() {
        evenAdapter.read(JsonReader(createGlobalUserBannedEventStringReader())) `should be equal to` globalUserBannedEvent
    }

    @Test
    fun `Should read UserDeletedEvent`() {
        evenAdapter.read(JsonReader(createUserDeletedEventStringReader())) `should be equal to` userDeletedEvent
    }

    @Test
    fun `Should read UserMutedEvent`() {
        evenAdapter.read(JsonReader(createUserMutedEventStringReader())) `should be equal to` userMutedEvent
    }

    @Test
    fun `Should read UsersMutedEvent`() {
        evenAdapter.read(JsonReader(createUsersMutedEventStringReader())) `should be equal to` usersMutedEvent
    }

    @Test
    fun `Should read UserPresenceChangedEvent`() {
        evenAdapter.read(JsonReader(createUserPresenceChangedEventStringReader())) `should be equal to` userPresenceChangedEvent
    }

    @Test
    fun `Should read UserStartWatchingEvent`() {
        evenAdapter.read(JsonReader(createUserStartWatchingEventStringReader())) `should be equal to` userStartWatchingEvent
    }

    @Test
    fun `Should read UserStopWatchingEvent`() {
        evenAdapter.read(JsonReader(createUserStopWatchingEventStringReader())) `should be equal to` userStopWatchingEvent
    }

    @Test
    fun `Should read ChannelUserUnbannedEvent`() {
        evenAdapter.read(JsonReader(createChannelUserUnbannedEventStringReader())) `should be equal to` channelUserUnbannedEvent
    }

    @Test
    fun `Should read GlobalUserUnbannedEvent`() {
        evenAdapter.read(JsonReader(createGlobalUserUnbannedEventStringReader())) `should be equal to` globalUserUnbannedEvent
    }

    @Test
    fun `Should read UserUnmutedEvent`() {
        evenAdapter.read(JsonReader(createUserUnmutedEventStringReader())) `should be equal to` userUnmutedEvent
    }

    @Test
    fun `Should read UsersUnmutedEvent`() {
        evenAdapter.read(JsonReader(createUsersUnmutedEventStringReader())) `should be equal to` usersUnmutedEvent
    }

    @Test
    fun `Should read UserUpdatedEvent`() {
        evenAdapter.read(JsonReader(createUserUpdatedEventStringReader())) `should be equal to` userUpdatedEvent
    }

    @Test
    fun `Should read ConnectedEvent`() {
        evenAdapter.read(JsonReader(createConnectedEventStringReader())) `should be equal to` connectedEvent
    }

    @Test
    fun `Should read ChannelCreatedEvent`() {
        evenAdapter.read(JsonReader(createChannelCreatedEventStringReader())) `should be equal to` channelCreatedEvent
    }

    @Test
    fun `Should read ChannelDeletedEvent`() {
        evenAdapter.read(JsonReader(createChannelDeletedEventStringReader())) `should be equal to` channelDeletedEvent
    }

    @Test
    fun `Should read ChannelHiddenEvent`() {
        evenAdapter.read(JsonReader(createChannelHiddenEventStringReader())) `should be equal to` channelHiddenEvent
    }

    @Test
    fun `Should read ChannelMuteEvent`() {
        evenAdapter.read(JsonReader(createChannelMuteEventStringReader())) `should be equal to` channelMuteEvent
    }

    @Test
    fun `Should read ChannelsMuteEvent`() {
        evenAdapter.read(JsonReader(createChannelsMuteEventStringReader())) `should be equal to` channelsMuteEvent
    }

    @Test
    fun `Should read HealthEvent`() {
        evenAdapter.read(JsonReader(createHealthEventStringReader())) `should be equal to` healthEvent
    }

    @Test
    fun `Should read NotificationChannelMutesUpdatedEvent`() {
        evenAdapter.read(JsonReader(createNotificationChannelMutesUpdatedEventStringReader())) `should be equal to` notificationChannelMutesUpdatedEvent
    }

    @Test
    fun `Should read NotificationMutesUpdatedEvent`() {
        evenAdapter.read(JsonReader(createNotificationMutesUpdatedEventStringReader())) `should be equal to` notificationMutesUpdatedEvent
    }

    @Test
    fun `Should read NewMessageEvent`() {
        evenAdapter.read(JsonReader(createNewMessageEventStringReader())) `should be equal to` newMessageEvent
    }
}
