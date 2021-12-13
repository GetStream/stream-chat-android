package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel

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
     *
     * @return [EventHandlingResult] Result of handling.
     */
    public fun handleChatEvent(event: ChatEvent, filter: FilterObject): EventHandlingResult
}

/** Class representing possible outcome of chat event handling. */
public sealed class EventHandlingResult {
    /**
     * Add a channel to a query channels collection.
     *
     * @param channel Channel to be added.
     */
    public class Add(public val channel: Channel) : EventHandlingResult()

    /**
     * Remove a channel from a query channels collection.
     *
     * @param cid cid of channel to remove.
     *
     */
    public class Remove(public val cid: String) : EventHandlingResult()

    /** Skip handling of this event. */
    public object Skip : EventHandlingResult()
}

/**
 * Basic implementation of [ChatEventHandler]. It handles basic channel events like [NotificationAddedToChannelEvent],
 * [ChannelDeletedEvent], [NotificationChannelDeletedEvent], [ChannelUpdatedByUserEvent], [ChannelUpdatedEvent].
 * It skips other type of events, mark as remove result [EventHandlingResult.REMOVE] for deleted events, other logic
 * you're free to implement.
 */
public abstract class BaseChatEventHandler : ChatEventHandler {
    /** Handles [NotificationAddedToChannelEvent] event. It runs in background. */
    public abstract fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult

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

    public open fun handleCidEvent(event: CidEvent, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is ChannelHiddenEvent -> EventHandlingResult.Remove(event.cid)
            else -> EventHandlingResult.Skip
        }
    }

    override fun handleChatEvent(event: ChatEvent, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is HasChannel -> handleChannelEvent(event, filter)
            is CidEvent -> handleCidEvent(event, filter)
            else -> EventHandlingResult.Skip
        }
    }
}
