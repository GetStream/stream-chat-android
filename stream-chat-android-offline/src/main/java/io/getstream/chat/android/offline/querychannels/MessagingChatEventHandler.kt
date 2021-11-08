package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.ChatDomain

/**
 * Default implementation of [ChatEventHandler] for the messaging type use-case. It is based on assumption when the
 * current user becomes a member this channel should be added, otherwise deleted.
 */
public class MessagingChatEventHandler : BaseChatEventHandler() {
    private val currentUser: User?
        get() = ChatDomain.instance().user.value

    override fun onNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult {
        return if (containsCurrentUser(event.channel)) {
            EventHandlingResult.ADD
        } else {
            EventHandlingResult.SKIP
        }
    }

    override fun onChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult {
        return if (containsCurrentUser(event.channel)) {
            EventHandlingResult.SKIP
        } else {
            EventHandlingResult.REMOVE
        }
    }

    override fun onChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult {
        return if (containsCurrentUser(event.channel)) {
            EventHandlingResult.SKIP
        } else {
            EventHandlingResult.REMOVE
        }
    }

    override fun onNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.ADD

    override fun onNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.REMOVE

    private fun containsCurrentUser(channel: Channel): Boolean {
        val _currentUser = currentUser
        return when {
            _currentUser == null -> false
            channel.members.any { it.user.id == _currentUser.id } -> true
            else -> false
        }
    }
}
