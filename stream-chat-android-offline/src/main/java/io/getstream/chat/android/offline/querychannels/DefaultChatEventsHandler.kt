package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

internal class DefaultChatEventsHandler(
    private val client: ChatClient,
    private val channels: StateFlow<List<Channel>>,
) : BaseChatEventsHandler() {
    internal val newChannelEventFilter: suspend (Channel, FilterObject) -> Boolean = { channel, filter ->
        client.queryChannels(
            QueryChannelsRequest(
                filter = Filters.and(
                    filter,
                    Filters.eq("cid", channel.cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
            .map { channels -> channels.any { it.cid == channel.cid } }
            .let { it.isSuccess && it.data() }
    }

    override fun onNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult =
        handleCidEventByRequest(event, filter)

    override fun onChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = EventHandlingResult.SKIP

    override fun onChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult =
        EventHandlingResult.SKIP

    /**
     * Handles [NotificationMessageNewEvent]. If the current channel list contains the channel from this event then it
     * returns [EventHandlingResult.SKIP] otherwise it makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationMessageNewEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun onNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult {
        return if (channels.value.any { it.cid == event.cid }) {
            EventHandlingResult.SKIP
        } else {
            handleCidEventByRequest(event, filter)
        }
    }

    private fun handleCidEventByRequest(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return runBlocking {
            if (newChannelEventFilter(event.channel, filter)) {
                EventHandlingResult.ADD
            } else {
                EventHandlingResult.REMOVE
            }
        }
    }
}
