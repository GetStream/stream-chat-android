package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.HasChannel
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.map
import kotlinx.coroutines.runBlocking

internal class DefaultChannelEventsHandler(
    private val client: ChatClient,
    private val filter: FilterObject,
) : BaseChannelEventsHandler() {
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

    override fun onNotificationAddedToChannelEvent(event: NotificationAddedToChannelEvent): EventHandlingResult =
        handleCidEventByRequest(event)

    override fun onChannelUpdatedByUserEvent(event: ChannelUpdatedByUserEvent): EventHandlingResult =
        handleCidEventByRequestIfNeeded(event)

    override fun onChannelUpdatedEvent(event: ChannelUpdatedEvent): EventHandlingResult =
        handleCidEventByRequestIfNeeded(event)

    private fun handleCidEventByRequestIfNeeded(event: HasChannel): EventHandlingResult {
        return if (checkFilterOnChannelUpdatedEvent) {
            handleCidEventByRequest(event)
        } else {
            EventHandlingResult.SKIP
        }
    }

    private fun handleCidEventByRequest(event: HasChannel): EventHandlingResult {
        return runBlocking {
            if (newChannelEventFilter(event.channel, filter)) {
                EventHandlingResult.ADD
            } else {
                EventHandlingResult.REMOVE
            }
        }
    }
}
