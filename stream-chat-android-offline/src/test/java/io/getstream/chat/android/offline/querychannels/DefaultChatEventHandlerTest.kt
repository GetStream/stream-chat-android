package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.events.NotificationMessageNewEvent
import io.getstream.chat.android.client.events.NotificationRemovedFromChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.offline.randomChannel
import io.getstream.chat.android.offline.randomMember
import io.getstream.chat.android.offline.randomMessage
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.randomInt
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class DefaultChatEventHandlerTest {

    @Test
    fun `When received NotificationAddedToChannelEvent and the channel is not present, it should be added`() {
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
    fun `When received NotificationAddedToChannelEvent and the channel is present, it should be skiped`() {
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
    fun `When received NotificationRemovedFromChannelEvent and the channel is not present, it should be Skip`() {
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
    fun `When received NotificationRemovedFromChannelEvent and the channel is present, it should be removed`() {
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
    fun `When received NotificationMessageNewEvent and the channel is absent, it should be added`() {
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
    fun `When received NotificationMessageNewEvent and the channel is present, it should be skiped`() {
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

    private fun randomNotificationAddedToChannelEvent(
        cid: String = randomString(),
        channel: Channel = randomChannel()
    ): NotificationAddedToChannelEvent {
        return NotificationAddedToChannelEvent(
            type = randomString(),
            createdAt = Date(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = channel,
            totalUnreadCount = randomInt(),
            unreadChannels = randomInt(),
        )
    }

    private fun randomNotificationRemovedFromChannelEvent(
        cid: String = randomString(),
        channel: Channel = randomChannel()
    ): NotificationRemovedFromChannelEvent {
        return NotificationRemovedFromChannelEvent(
            type = randomString(),
            user = randomUser(),
            createdAt = Date(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = channel,
            member = randomMember()
        )
    }

    private fun randomNotificationMessageNewEvent(
        cid: String = randomString(),
        channel: Channel = randomChannel()
    ): NotificationMessageNewEvent {
        return NotificationMessageNewEvent(
            type = randomString(),
            createdAt = Date(),
            cid = cid,
            channelType = randomString(),
            channelId = randomString(),
            channel = channel,
            message = randomMessage(),
            totalUnreadCount = randomInt(),
            unreadChannels = randomInt()
        )
    }
}
