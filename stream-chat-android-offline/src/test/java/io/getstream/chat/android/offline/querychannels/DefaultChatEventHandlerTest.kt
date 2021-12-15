package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomNotificationAddedToChannelEvent
import io.getstream.chat.android.offline.randomNotificationMessageNewEvent
import io.getstream.chat.android.offline.randomNotificationRemovedFromChannelEvent
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class DefaultChatEventHandlerTest {

    @Test
    fun `Given the channel is not present, When received NotificationAddedToChannelEvent, Should channel be added`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomNotificationAddedToChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationAddedToChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given the channel is present, When received NotificationAddedToChannelEvent, Should Event be skiped`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomNotificationAddedToChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationAddedToChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is not present, When received NotificationRemovedFromChannelEvent, Should channel be removed`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomNotificationRemovedFromChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationRemovedFromChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }

    @Test
    fun `Given the channel is present, When received NotificationRemovedFromChannelEvent, Should channel be removed`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomNotificationRemovedFromChannelEvent(cid, channel)

        val result = eventHandler.handleNotificationRemovedFromChannelEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Remove(cid)
    }

    @Test
    fun `Given the channel is not present, When received NotificationMessageNewEvent, Should channel be added`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(emptyList()))

        val event = randomNotificationMessageNewEvent(cid, channel)

        val result = eventHandler.handleNotificationMessageNewEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Add(channel)
    }

    @Test
    fun `Given the channel is not present, When received NotificationMessageNewEvent, Should event be skiped`() {
        val cid = randomString()
        val channel = randomChannel(cid = cid)
        val eventHandler = DefaultChatEventHandler(MutableStateFlow(listOf(channel)))

        val event = randomNotificationMessageNewEvent(cid, channel)

        val result = eventHandler.handleNotificationMessageNewEvent(
            event,
            Filters.neutral()
        )

        result `should be equal to` EventHandlingResult.Skip
    }
}
