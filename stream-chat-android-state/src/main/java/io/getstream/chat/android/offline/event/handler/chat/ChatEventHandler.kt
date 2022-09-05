/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

@file:Suppress("DEPRECATION_ERROR")

package io.getstream.chat.android.offline.event.handler.chat

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelDeletedEvent
import io.getstream.chat.android.client.events.ChannelHiddenEvent
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.ChannelVisibleEvent
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

/**
 * Handler responsible for deciding whether the set of channels should be updated after receiving the particular event.
 *
 * @see [EventHandlingResult]
 */
public fun interface ChatEventHandler {
    /**
     * Computes the event handling result.
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.
     * @param cachedChannel optional cached [Channel] object.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    public fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult
}

/**
 * Represent possible outcomes of handling a chat event.
 */
public sealed class EventHandlingResult {
    /**
     * Add a channel to a query channels collection.
     *
     * @param channel Channel to be added.
     */
    public data class Add(public val channel: Channel) : EventHandlingResult()

    /**
     * Call watch and add the channel to a query channels collection.
     *
     * @param cid cid of the channel to watch and add.
     */
    public data class WatchAndAdd(public val cid: String) : EventHandlingResult()

    /**
     * Remove a channel from a query channels collection.
     *
     * @param cid cid of channel to remove.
     *
     */
    public data class Remove(public val cid: String) : EventHandlingResult()

    /**
     * Skip the event.
     */
    public object Skip : EventHandlingResult()
}

/**
 * More specific [ChatEventHandler] implementation that gives you a separation
 * between [CidEvent] and [HasChannel] events.
 *
 * The channel will be removed from the set after receiving
 * [ChannelDeletedEvent], [NotificationChannelDeletedEvent], [ChannelHiddenEvent] events.
 *
 * The channel will be watched and added to the set after receiving [ChannelVisibleEvent] event.
 *
 * Other events will be skipped.
 */
public abstract class BaseChatEventHandler : ChatEventHandler {
    @Deprecated(
        message = "Use handleChatEvent() instead.",
        replaceWith = ReplaceWith("this.handleChatEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [NotificationAddedToChannelEvent] event. It runs in background. */
    public abstract fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult

    @Deprecated(
        message = "Use handleCidEvent() instead.",
        replaceWith = ReplaceWith("this.handleCidEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [MemberAddedEvent] event. It runs in background. */
    public open fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = EventHandlingResult.Skip

    @Deprecated(
        message = "Use handleCidEvent() instead.",
        replaceWith = ReplaceWith("this.handleCidEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [MemberRemovedEvent] event. It runs in background. */
    public open fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = EventHandlingResult.Skip

    @Deprecated(
        message = "Use handleChatEvent() instead.",
        replaceWith = ReplaceWith("this.handleChatEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [ChannelUpdatedByUserEvent] event. It runs in background. */
    public abstract fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult

    @Deprecated(
        message = "Use handleChatEvent() instead.",
        replaceWith = ReplaceWith("this.handleChatEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [ChannelUpdatedEvent] event. It runs in background. */
    public abstract fun handleChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult

    /**
     * Handles [ChannelVisibleEvent] event.
     * By default returns [EventHandlingResult.WatchAndAdd].
     *
     * @param event [ChannelVisibleEvent] to handle.
     * @param filter [FilterObject] for query channels collection.
     */
    @Deprecated(
        message = "Use handleCidEvent() instead.",
        replaceWith = ReplaceWith("this.handleCidEvent()"),
        level = DeprecationLevel.ERROR,
    )
    public open fun handleChannelVisibleEvent(
        event: ChannelVisibleEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.WatchAndAdd(event.cid)

    @Deprecated(
        message = "Use handleChatEvent() instead.",
        replaceWith = ReplaceWith("this.handleChatEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [NotificationMessageNewEvent] event. It runs in background. */
    public open fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.WatchAndAdd(event.cid)

    @Deprecated(
        message = "Use handleChatEvent() instead.",
        replaceWith = ReplaceWith("this.handleChatEvent()"),
        level = DeprecationLevel.ERROR,
    )
    /** Handles [NotificationRemovedFromChannelEvent] event. It runs in background. */
    public open fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [HasChannel] event which contains specific [Channel] object.
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.

     * @return [EventHandlingResult] Result of handling.
     */
    public open fun handleChannelEvent(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return when (event) {
            is ChannelDeletedEvent -> EventHandlingResult.Remove(event.cid)
            is NotificationChannelDeletedEvent -> EventHandlingResult.Remove(event.cid)
            is NotificationAddedToChannelEvent -> handleNotificationAddedToChannelEvent(event, filter)
            is NotificationRemovedFromChannelEvent -> handleNotificationRemovedFromChannelEvent(event, filter)
            is ChannelUpdatedByUserEvent -> handleChannelUpdatedByUserEvent(event, filter)
            is ChannelUpdatedEvent -> handleChannelUpdatedEvent(event, filter)
            is NotificationMessageNewEvent -> handleNotificationMessageNewEvent(event, filter)
            else -> EventHandlingResult.Skip
        }
    }

    /**
     * Handles [CidEvent] event which is associated with a specific [Channel] which can be tracked using [CidEvent.cid].
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.
     * @param cachedChannel optional cached [Channel] object if exists.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    public open fun handleCidEvent(
        event: CidEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult {
        return when (event) {
            is ChannelHiddenEvent -> EventHandlingResult.Remove(event.cid)
            is ChannelVisibleEvent -> handleChannelVisibleEvent(event, filter)
            is MemberRemovedEvent -> handleMemberRemovedEvent(event, filter, cachedChannel)
            is MemberAddedEvent -> handleMemberAddedEvent(event, filter, cachedChannel)
            else -> EventHandlingResult.Skip
        }
    }

    /**
     * Computes the event handling result.
     *
     * @param event [ChatEvent] that may contain updates for the set of channels.
     * @param filter [FilterObject] associated with the set of channels. Can be used to define the result of handling.
     * @param cachedChannel optional cached [Channel] object.
     *
     * @return [EventHandlingResult] Result of handling.
     */
    override fun handleChatEvent(event: ChatEvent, filter: FilterObject, cachedChannel: Channel?): EventHandlingResult {
        return when (event) {
            is HasChannel -> handleChannelEvent(event, filter)
            is CidEvent -> handleCidEvent(event, filter, cachedChannel)
            else -> EventHandlingResult.Skip
        }
    }
}
