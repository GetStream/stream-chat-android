package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent

/**
 * Events handler that handles events related to channels and compute which kind of action should be applied.
 */
public fun interface ChannelEventsHandler {
    /**
     * Function that computes result of handling event.
     *
     * @param event Event that contains particular channel. See more [HasChannel]
     * @return [EventHandlingResult] Result of handling.
     */
    public suspend fun onChannelEvent(event: HasChannel): EventHandlingResult
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

public abstract class BaseChannelEventsHandler : ChannelEventsHandler {
    public abstract suspend fun onNotificationAddedToChannelEvent(event: NotificationAddedToChannelEvent): EventHandlingResult
    public abstract suspend fun onChannelUpdatedByUserEvent(event: ChannelUpdatedByUserEvent): EventHandlingResult
    public abstract suspend fun onChannelUpdatedEvent(event: ChannelUpdatedEvent): EventHandlingResult

    override suspend fun onChannelEvent(event: HasChannel): EventHandlingResult {
        return when (event) {
            is NotificationAddedToChannelEvent -> onNotificationAddedToChannelEvent(event)
            is ChannelDeletedEvent -> EventHandlingResult.REMOVE
            is NotificationChannelDeletedEvent -> EventHandlingResult.REMOVE
            is ChannelUpdatedByUserEvent -> onChannelUpdatedByUserEvent(event)
            is ChannelUpdatedEvent -> onChannelUpdatedEvent(event)
            else -> EventHandlingResult.SKIP
        }
    }
}
