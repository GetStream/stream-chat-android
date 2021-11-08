package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

/**
 * Default implementation of [ChatEventHandler] which is more generic than [MessagingChatEventHandler]. It handles the
 * most obvious events
 */
public open class DefaultChatEventHandler(
    private val client: ChatClient,
    private val channels: StateFlow<List<Channel>>,
) : BaseChatEventHandler() {
    internal val newChannelEventFilter: suspend (String, FilterObject) -> Boolean = { cid, filter ->
        client.queryChannels(
            QueryChannelsRequest(
                filter = Filters.and(
                    filter,
                    Filters.eq("cid", cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
            .map { channels -> channels.any { it.cid == cid } }
            .let { it.isSuccess && it.data() }
    }

    override fun onNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult =
        handleCidEventByRequest(event.cid, filter)

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
            handleCidEventByRequest(event.cid, filter)
        }
    }

    /**
     * Handles [NotificationRemovedFromChannelEvent]. If the current channel list doesn't contain a channel from the
     * event this handler returns the skip result, otherwise it makes a request to API to define outcome of handling.
     *
     * @param event Instance of [NotificationRemovedFromChannelEvent] that is being handled.
     * @param filter [FilterObject] which is used to define an outcome.
     */
    override fun onNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult {
        return if (channels.value.any { it.cid == event.cid }.not()) {
            EventHandlingResult.SKIP
        } else {
            handleCidEventByRequest(event.cid, filter)
        }
    }

    private fun handleCidEventByRequest(cid: String, filter: FilterObject): EventHandlingResult {
        return runBlocking {
            if (newChannelEventFilter(cid, filter)) {
                EventHandlingResult.ADD
            } else {
                EventHandlingResult.REMOVE
            }
        }
    }
}
