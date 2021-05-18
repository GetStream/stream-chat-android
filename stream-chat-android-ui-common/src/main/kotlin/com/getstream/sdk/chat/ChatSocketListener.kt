package com.getstream.sdk.chat

import com.getstream.sdk.chat.enums.OnlineStatus
import io.getstream.chat.android.client.errors.ChatError
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
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
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
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.SocketListener
import io.getstream.chat.android.core.internal.exhaustive

internal class ChatSocketListener(
    private val onOnlineStatusListener: (OnlineStatus) -> Unit,
    private val onMeListener: (User) -> Unit,
    private val onTotalUnreadCountListener: (Int) -> Unit,
    private val onUnreadChannels: (Int) -> Unit,
) : SocketListener() {
    override fun onConnected(event: ConnectedEvent) {
        onMeListener(event.me)
        onOnlineStatusListener(OnlineStatus.CONNECTED)
    }

    override fun onConnecting() {
        onOnlineStatusListener(OnlineStatus.CONNECTING)
    }

    override fun onError(error: ChatError) {
        onOnlineStatusListener(OnlineStatus.FAILED)
    }

    override fun onEvent(event: ChatEvent) {
        when (event) {
            is NewMessageEvent -> {
                onTotalUnreadCountListener(event.totalUnreadCount)
                onUnreadChannels(event.unreadChannels)
            }
            is NotificationMarkReadEvent -> {
                onTotalUnreadCountListener(event.totalUnreadCount)
                onUnreadChannels(event.unreadChannels)
            }
            is NotificationMessageNewEvent -> {
                onTotalUnreadCountListener(event.totalUnreadCount)
                onUnreadChannels(event.unreadChannels)
            }
            is MarkAllReadEvent -> {
                onTotalUnreadCountListener(event.totalUnreadCount)
                onUnreadChannels(event.unreadChannels)
            }
            is ConnectedEvent -> {
                onConnected(event)
            }
            is ChannelTruncatedEvent,
            is ChannelDeletedEvent,
            is ChannelHiddenEvent,
            is ChannelUpdatedEvent,
            is ChannelUpdatedByUserEvent,
            is ChannelVisibleEvent,
            is MemberAddedEvent,
            is MemberRemovedEvent,
            is MemberUpdatedEvent,
            is MessageDeletedEvent,
            is MessageReadEvent,
            is MessageUpdatedEvent,
            is NotificationAddedToChannelEvent,
            is NotificationChannelDeletedEvent,
            is NotificationChannelTruncatedEvent,
            is NotificationInviteAcceptedEvent,
            is NotificationInviteRejectedEvent,
            is NotificationInvitedEvent,
            is NotificationRemovedFromChannelEvent,
            is ReactionDeletedEvent,
            is ReactionNewEvent,
            is ReactionUpdateEvent,
            is TypingStartEvent,
            is TypingStopEvent,
            is ChannelUserBannedEvent,
            is UserStartWatchingEvent,
            is UserStopWatchingEvent,
            is ChannelUserUnbannedEvent,
            is HealthEvent,
            is NotificationChannelMutesUpdatedEvent,
            is NotificationMutesUpdatedEvent,
            is GlobalUserBannedEvent,
            is UserDeletedEvent,
            is UserPresenceChangedEvent,
            is GlobalUserUnbannedEvent,
            is UserUpdatedEvent,
            is ConnectingEvent,
            is DisconnectedEvent,
            is ErrorEvent,
            is UnknownEvent,
            -> Unit
        }.exhaustive
    }
}
