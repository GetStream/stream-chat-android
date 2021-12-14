package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.runBlocking

/**
 * Implementation of [ChatEventHandler] that handles events when the of channels shows which
 * channels the current user is not a member. BE AWARE that this implementation uses more API calls than [DefaultChatEventHandler]
 */
public class NonMemberChatEventHandler(
    private val client: ChatClient,
    private val channels: List<Channel>,
) : BaseChatEventHandler() {

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
    ): EventHandlingResult {
        val hasChannel = channels.any { it.cid == event.cid }

        return if (hasChannel) EventHandlingResult.Remove(event.cid) else EventHandlingResult.Skip
    }

    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    override fun handleChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult =
        EventHandlingResult.Skip

    /**
     * Handles [NotificationMessageNewEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationMessageNewEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    /**
     * Handles [MemberRemovedEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [MemberRemovedEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
    ): EventHandlingResult {
        val channel = runBlocking {
            val request = ChannelFilterRequest.filter(client, event.cid, filter)
            if (request.isSuccess) {
                request.data().find { channel -> channel.cid == event.cid }
            } else {
                null
            }
        }

        val hasChannel = channels.any { it.cid == event.cid }

        return if (!hasChannel && channel != null) EventHandlingResult.Add(channel) else EventHandlingResult.Skip
    }

    /**
     * Handles [NotificationRemovedFromChannelEvent]. It makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip
}
