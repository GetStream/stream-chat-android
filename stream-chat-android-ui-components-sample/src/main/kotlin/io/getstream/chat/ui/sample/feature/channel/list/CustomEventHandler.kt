package io.getstream.chat.ui.sample.feature.channel.list

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.events.ChannelUpdatedByUserEvent
import io.getstream.chat.android.client.events.ChannelUpdatedEvent
import io.getstream.chat.android.client.events.MemberAddedEvent
import io.getstream.chat.android.client.events.MemberRemovedEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.querychannels.BaseChatEventHandler
import io.getstream.chat.android.offline.querychannels.EventHandlerFactory
import io.getstream.chat.android.offline.querychannels.EventHandlingResult
import io.getstream.chat.ui.sample.common.isDraft
import kotlinx.coroutines.flow.StateFlow

class CustomChatEventHandlerFactory : EventHandlerFactory {
    override fun chatEventHandler(channels: StateFlow<List<Channel>>) = CustomChatEventHandler(channels)
}

class CustomChatEventHandler(private val channels: StateFlow<List<Channel>>) : BaseChatEventHandler() {

    override fun handleNotificationAddedToChannelEvent(
        event: NotificationAddedToChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, event.channel)

    override fun handleMemberAddedEvent(
        event: MemberAddedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, cachedChannel)

    override fun handleChannelUpdatedByUserEvent(
        event: ChannelUpdatedByUserEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, event.channel)

    override fun handleChannelUpdatedEvent(
        event: ChannelUpdatedEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, event.channel)

    override fun handleNotificationMessageNewEvent(
        event: NotificationMessageNewEvent,
        filter: FilterObject,
    ): EventHandlingResult = addIfChannelIsAbsentAndNotDraft(channels, event.channel)

    override fun handleMemberRemovedEvent(
        event: MemberRemovedEvent,
        filter: FilterObject,
        cachedChannel: Channel?,
    ): EventHandlingResult = removeIfChannelIsPresent(channels, cachedChannel)

    override fun handleNotificationRemovedFromChannelEvent(
        event: NotificationRemovedFromChannelEvent,
        filter: FilterObject,
    ): EventHandlingResult = removeIfChannelIsPresent(channels, event.channel)

    private fun addIfChannelIsAbsentAndNotDraft(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
        return if (channel == null || channel.isDraft || channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Skip
        } else {
            EventHandlingResult.Add(channel)
        }
    }

    private fun removeIfChannelIsPresent(channels: StateFlow<List<Channel>>, channel: Channel?): EventHandlingResult {
        return if (channel != null && channels.value.any { it.cid == channel.cid }) {
            EventHandlingResult.Remove(channel.cid)
        } else {
            EventHandlingResult.Skip
        }
    }
}
