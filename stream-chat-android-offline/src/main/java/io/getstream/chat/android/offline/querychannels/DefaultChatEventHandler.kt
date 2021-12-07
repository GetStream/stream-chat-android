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
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.map
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking

/**
 * Default implementation of [ChatEventHandler] which is more generic than [MessagingChatEventHandler]. It skips updates
 * and makes an API request if a channel wasn't yet handled before when receives [NotificationAddedToChannelEvent],
 * [NotificationMessageNewEvent], [NotificationRemovedFromChannelEvent].
 */
internal class DefaultChatEventHandler(private val client: ChatClient, private val channels: StateFlow<List<Channel>>) :
    BaseChatEventHandler() {

    /**
     * Channel filter function. It makes an API query channel request based on cid of a channel and a filter object to
     * define should be the channel with such cid be in the list of channels or not.
     */
    internal val channelFilter: suspend (cid: String, FilterObject) -> Boolean = { cid, filter ->
        client.queryChannelsInternal(
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
            .let { filteringResult -> filteringResult.isSuccess && filteringResult.data() }
    }

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = handleMemberUpdate(event, event.cid, filter)

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
    ): EventHandlingResult = fireRequestIfChannelIsAbsent(event.channel, filter)

    /**
     * Checks if the channel collection contains a channel, if yes then it returns skip handling result, otherwise it
     * fires request by [channelFilter] to define outcome of handling.
     */
    private fun fireRequestIfChannelIsAbsent(channel: Channel, filter: FilterObject): EventHandlingResult {
        return if (channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Skip
        } else {
            checkCidByChannelFilter(channel.cid, filter, EventHandlingResult.Add(channel))
        }
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
    ): EventHandlingResult = handleMemberUpdate(event, event.cid, filter)

    private fun handleMemberUpdate(
        event: HasChannel,
        cid: String,
        filter: FilterObject,
    ): EventHandlingResult {
        val channel = event.channel

        return runBlocking {
            val hasChannel = channels.value.any { it.cid == channel.cid }
            val filterPassed = channelFilter(channel.cid, filter)

            when {
                filterPassed && !hasChannel -> EventHandlingResult.Add(channel)

                !filterPassed && hasChannel -> EventHandlingResult.Remove(cid)

                else -> EventHandlingResult.Skip
            }
        }
    }

    /**
     * Run filter request. If filter is passed then it returns [filterPositiveResult], otherwise it returns [EventHandlingResult.Remove].
     */
    private fun checkCidByChannelFilter(
        cid: String,
        filter: FilterObject,
        filterPositiveResult: EventHandlingResult,
    ): EventHandlingResult {
        return runBlocking {
            if (channelFilter(cid, filter)) {
                filterPositiveResult
            } else {
                EventHandlingResult.Remove(cid)
            }
        }
    }
}
