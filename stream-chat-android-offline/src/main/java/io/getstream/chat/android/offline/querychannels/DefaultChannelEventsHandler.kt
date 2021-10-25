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
import kotlinx.coroutines.runBlocking

internal class DefaultChannelEventsHandler(private val client: ChatClient) : BaseChannelEventsHandler() {
    internal var checkFilterOnChannelUpdatedEvent: Boolean = false

    internal var newChannelEventFilter: suspend (Channel, FilterObject) -> Boolean = { channel, filter ->
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
    ): EventHandlingResult =
        handleCidEventByRequestIfNeeded(event, filter)

    override fun onChannelUpdatedEvent(event: ChannelUpdatedEvent, filter: FilterObject): EventHandlingResult =
        handleCidEventByRequestIfNeeded(event, filter)

    override fun onNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleCidEventByRequest(event, filter)

    private fun handleCidEventByRequestIfNeeded(event: HasChannel, filter: FilterObject): EventHandlingResult {
        return if (checkFilterOnChannelUpdatedEvent) {
            handleCidEventByRequest(event, filter)
        } else {
            EventHandlingResult.SKIP
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
