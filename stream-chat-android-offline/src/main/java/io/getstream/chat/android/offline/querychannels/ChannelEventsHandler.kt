package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent

/**
 * Interface that handles events related to the particular set of channels. These channels correspond to particular [FilterObject].
 * Events handler computes which kind of action [EventHandlingResult] should be applied to this set.
 */
public fun interface ChannelEventsHandler {
    /**
     * Function that computes result of handling event. It runs in background.
     *
     * @param event Event that contains particular channel. See more [HasChannel]
     * @param filter [FilterObject] that can be used to define result of handling.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    public fun onChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult
}

/**
 * Enum representing possible outcome of channels event handling.
 */
public enum class EventHandlingResult {
    /**
     * Add a channel to a query channels collection.
     */
    ADD,

    /**
     * Remove a channel from a query channels collection.
     */
    REMOVE,

    /**
     * Skip handling of this event.
     */
    SKIP
}

/**
 * Basic implementation of [ChannelEventsHandler]. It handles basic channel events like [NotificationAddedToChannelEvent],
 * [ChannelDeletedEvent], [NotificationChannelDeletedEvent], [ChannelUpdatedByUserEvent], [ChannelUpdatedEvent].
 * It skips other type of events, mark as remove result [EventHandlingResult.REMOVE] for deleted events, other logic
 * you're free to implement.
 */
public abstract class BaseChannelEventsHandler : ChannelEventsHandler {
    /**
     * Handles [NotificationAddedToChannelEvent] event. It runs in background.
     */
    public abstract fun onNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult

    /**
     * Handles [ChannelUpdatedByUserEvent] event. It runs in background.
     */
    public abstract fun onChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult

    /**
     * Handles [ChannelUpdatedEvent] event. It runs in background.
     */
    public abstract fun onChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult

    override fun onChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is NotificationAddedToChannelEvent -> onNotificationAddedToChannelEvent(event, filter)
            is ChannelDeletedEvent -> EventHandlingResult.REMOVE
            is NotificationChannelDeletedEvent -> EventHandlingResult.REMOVE
            is ChannelUpdatedByUserEvent -> onChannelUpdatedByUserEvent(event, filter)
            is ChannelUpdatedEvent -> onChannelUpdatedEvent(event, filter)
            else -> EventHandlingResult.SKIP
        }
    }
}
