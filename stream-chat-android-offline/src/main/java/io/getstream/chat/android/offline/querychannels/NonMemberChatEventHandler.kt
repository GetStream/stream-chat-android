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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

/**
 * Implementation of [ChatEventHandler] that handles events when ChannelListViewModel has a filter to show channels which
 * the current user is not a member. BE AWARE that this implementation uses much more API calls than [DefaultChatEventHandler]
 */
public class NonMemberChatEventHandler(
    private val client: ChatClient,
    private val channels: StateFlow<List<Channel>>,
) : BaseChatEventHandler() {

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.Skip

    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleMemberUpdate(event.cid, filter)

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
    ): EventHandlingResult = handleMemberUpdate(event.cid, filter)

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

    private fun handleMemberUpdate(
        cid: String,
        filter: FilterObject,
    ): EventHandlingResult {
        val channel = runBlocking {
            val request = ChannelFilterRequest.filter(client, cid, filter)
            if (request.isSuccess) {
                request.data().find { channel -> channel.cid == cid }
            } else {
                null
            }
        }

        val hasChannel = channels.value.any { it.cid == cid }
        val filterPassed = channel != null

        return parseEventResult(hasChannel, filterPassed, channel, cid)
    }

    private fun parseEventResult(
        hasChannel: Boolean,
        filterPassed: Boolean,
        channel: Channel?,
        cid: String,
    ): EventHandlingResult =
        when {
            filterPassed && !hasChannel && channel != null -> EventHandlingResult.Add(channel)

            !filterPassed && hasChannel -> EventHandlingResult.Remove(cid)

            else -> EventHandlingResult.Skip
        }
}
