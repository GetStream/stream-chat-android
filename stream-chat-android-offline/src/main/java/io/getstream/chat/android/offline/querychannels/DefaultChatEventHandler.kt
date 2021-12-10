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
 * Default implementation of [ChatEventHandler] which is more generic than [MessagingChatEventHandler]. It skips updates
 * and makes an API request if a channel wasn't yet handled before when receives [NotificationAddedToChannelEvent],
 * [NotificationMessageNewEvent], [NotificationRemovedFromChannelEvent].
 */
public class DefaultChatEventHandler(private val channels: StateFlow<List<Channel>>) :
    BaseChatEventHandler() {

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsent(event.channel)

    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

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
     * fires request by [channelFilter] to define outcome of handling.
     */
    private fun addIfChannelIsAbsent(channel: Channel): EventHandlingResult {
        return if (channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.Add(channel)
        }
    }

    private fun removeIfChannelIsPresent(channel: Channel): EventHandlingResult {
        return if (channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Remove(channel.cid)
        } else {
            EventHandlingResult.Skip
        }
    }
}
