package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

/**
 * Default implementation of [ChatEventHandler] which covers the default filter of channels.
 * The channel will be added if the current user is a member and it will be removed otherwise.
 *
 * @param channels The list of visible channels.
 */
public class DefaultChatEventHandler(private val channels: StateFlow<List<Channel>>) :
    BaseChatEventHandler() {

    /**
     *  Handles [NotificationAddedToChannelEvent] event. It adds the channel if it is absent.
     *
     * @param event Instance of [NotificationAddedToChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsent(channels, event.channel)

    /**
     *  Handles [MemberAddedEvent] event. It adds the channel if it is absent.
     *
     * @param event Instance of [NotificationAddedToChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     * @param cachedChannel optional [Channel] object cached in database.
     */
    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = addIfChannelIsAbsent(channels, cachedChannel)

    /** Handles [ChannelUpdatedByUserEvent] event. The event is skipped. */
    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [ChannelUpdatedEvent] event. The event is skipped. */
    override fun handleChannelUpdatedEvent(
        event: ChannelUpdatedEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [NotificationMessageNewEvent]. It adds the channel, if it is absent.
     *
     * @param event Instance of [NotificationMessageNewEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsent(channels, event.channel)

    /**
     * Handles [MemberRemovedEvent]. It removes the channel if it's present in the list.
     *
     * @param event Instance of [MemberRemovedEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     * @param cachedChannel optional [Channel] object cached in database
     */
    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = removeIfChannelIsPresent(channels, cachedChannel)

    /**
     * Handles [NotificationRemovedFromChannelEvent]. It removes the channel if it's present in the list.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = removeIfChannelIsPresent(channels, event.channel)
}
