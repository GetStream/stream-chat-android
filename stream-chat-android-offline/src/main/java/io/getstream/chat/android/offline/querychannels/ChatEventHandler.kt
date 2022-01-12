package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface that handles events related to the particular set of channels. These channels correspond to particular [FilterObject].
 * Events handler computes which kind of action [EventHandlingResult] should be applied to this set.
 */
public fun interface ChatEventHandler {
    /**
     * Function that computes result of handling event. It runs in background.
     *
     * @param event ChatEvent that may contain updates for the set of channels. See more [ChatEvent]
     * @param filter [FilterObject] that can be used to define result of handling.
     * @param cachedChannel optional [Channel] object cached in database
     *
     * @return [EventHandlingResult] Result of handling.
     */
    public fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult
}

/** Class representing possible outcome of chat event handling. */
public sealed class EventHandlingResult {
    /**
     * Add a channel to a query channels collection.
     *
     * @param channel Channel to be added.
     */
    public data class Add(public val channel: Channel) : EventHandlingResult()

    /**
     * Remove a channel from a query channels collection.
     *
     * @param cid cid of channel to remove.
     *
     */
    public data class Remove(public val cid: String) : EventHandlingResult()

    /** Skip handling of this event. */
    public object Skip : EventHandlingResult()
}

/**
 * Basic implementation of [ChatEventHandler]. It handles following channel events: [NotificationAddedToChannelEvent],
 * [MemberAddedEvent], [NotificationRemovedFromChannelEvent], [MemberRemovedEvent], [ChannelUpdatedByUserEvent],
 * [ChannelUpdatedEvent], [NotificationMessageNewEvent].
 */
public abstract class BaseChatEventHandler : ChatEventHandler {
    /** Handles [NotificationAddedToChannelEvent] event. It runs in background. */
    public abstract fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult

    /** Handles [MemberAddedEvent] event. It runs in background. */
    public open fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [MemberRemovedEvent] event. It runs in background. */
    public open fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [ChannelUpdatedByUserEvent] event. It runs in background. */
    public abstract fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult

    /** Handles [ChannelUpdatedEvent] event. It runs in background. */
    public abstract fun handleChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult

    /** Handles [NotificationMessageNewEvent] event. It runs in background. */
    public open fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [NotificationRemovedFromChannelEvent] event. It runs in background. */
    public open fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    public open fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is NotificationAddedToChannelEvent -> handleNotificationAddedToChannelEvent(event, filter)
            is NotificationRemovedFromChannelEvent -> handleNotificationRemovedFromChannelEvent(event, filter)
            is ChannelDeletedEvent -> EventHandlingResult.Remove(event.cid)
            is NotificationChannelDeletedEvent -> EventHandlingResult.Remove(event.cid)
            is ChannelUpdatedByUserEvent -> handleChannelUpdatedByUserEvent(event, filter)
            is ChannelUpdatedEvent -> handleChannelUpdatedEvent(event, filter)
            is NotificationMessageNewEvent -> handleNotificationMessageNewEvent(event, filter)
            else -> EventHandlingResult.Skip
        }
    }

    public open fun handleCidEvent(
        event: CidEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        return when (event) {
            is ChannelHiddenEvent -> EventHandlingResult.Remove(event.cid)
            is MemberRemovedEvent -> handleMemberRemovedEvent(event, filter, cachedChannel)
            is MemberAddedEvent -> handleMemberAddedEvent(event, filter, cachedChannel)
            else -> EventHandlingResult.Skip
        }
    }

    override fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is HasChannel -> handleChannelEvent(event, filter)
            is CidEvent -> handleCidEvent(event, filter, cachedChannel)
            else -> EventHandlingResult.Skip
        }
    }
}

/**
 * Checks if the channel collection contains a channel, if yes then it returns skip handling result, otherwise it
 * adds the channel.
 */
internal fun addIfChannelIsAbsent(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
    return if (channel == null || channels.value.any { it.cid == channel.cid }) {
        EventHandlingResult.Skip
    } else {
        EventHandlingResult.Add(channel)
    }
}

/**
 * Checks if the channel collection contains a channel, if yes then it removes it. Otherwise, it simply skips the event.
 */
internal fun removeIfChannelIsPresent(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
    return if (channel != null && channels.value.any { it.cid == channel.cid }) {
        EventHandlingResult.Remove(channel.cid)
    } else {
        EventHandlingResult.Skip
    }
}
