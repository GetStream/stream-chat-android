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
 * It  adds or removes channels accordingly with the notification event received. Events handled are: [NotificationAddedToChannelEvent],
 * [NotificationMessageNewEvent], [NotificationRemovedFromChannelEvent].
 * This Handler will skip the even if the channel is already added or absent and no interaction is needed.
 *
 * This handler expects that the list of channels are the channels that we user is a member.
 */
public class DefaultChatEventHandler(private val channels: StateFlow<List<Channel>>) :
    BaseChatEventHandler() {

    /** Handles [NotificationAddedToChannelEvent] event. It adds the channel, if it is absent. */
    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsent(event.channel)

    /** Handles [MemberAddedEvent] event. The event is skipped and should not arrive to this handler. */
    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [ChannelUpdatedByUserEvent] event. The event is skipped. */
    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /** Handles [ChannelUpdatedEvent] event. The event is skipped. */
    override fun handleChannelUpdatedEvent(
        event: ChannelUpdatedEvent,
        filter: FilterObject
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [NotificationMessageNewEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationMessageNewEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsent(event.channel)

    /**
     * Handles [MemberRemovedEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [MemberRemovedEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [NotificationRemovedFromChannelEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = removeIfChannelIsPresent(event.channel)

    /**
     * Checks if the channel collection contains a channel, if yes then it returns skip handling result, otherwise it
     * adds the channel.
     */
    private fun addIfChannelIsAbsent(channel: Channel): EventHandlingResult {
        return if (channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.Add(channel)
        }
    }

    /**
     * Checks if the channel collection contains a channel, if yes then it removes it. Otherwise it simply skips the event
     */
    private fun removeIfChannelIsPresent(channel: Channel): EventHandlingResult {
        return if (channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Remove(channel.cid)
        } else {
            EventHandlingResult.Skip
        }
    }
}
