package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.socket.EventsParser
import io.getstream.chat.android.client.utils.observable.FakeSocketService
import okhttp3.WebSocket
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

internal class EventsParserTests {

    private val socket = Mockito.mock(WebSocket::class.java)
    private lateinit var eventsCollector: MutableList<ChatEvent>
    private lateinit var service: FakeSocketService
    private lateinit var parser: EventsParser
    private val userId = "hello-user"
    private val eventType = EventType.HEALTH_CHECK

    @Before
    fun before() {
        eventsCollector = mutableListOf()
        service = FakeSocketService(eventsCollector)
        parser = EventsParser(GsonChatParser())
        parser.setSocketService(service)
    }

    @Test
    fun firstConnection() {
        parser.onMessage(socket, "{type: ${ EventType.HEALTH_CHECK }, me:{id:\"$userId\"}}")

        service.verifyConnectionUserId(userId)
    }

    @Test
    fun firstInvalidEvent() {
        parser.onMessage(socket, "{type:\"$eventType\"}")

        service.verifyNoConnectionUserId()
    }

    @Test
    fun mapTypesToObjects() {

        parser.onMessage(socket, "{me:{id:\"hello\"}, type: ${EventType.HEALTH_CHECK}}")

        parser.onMessage(socket, "{type: ${EventType.CHANNEL_TRUNCATED}}")
        parser.onMessage(socket, "{type: ${EventType.NOTIFICATION_CHANNEL_TRUNCATED}}")
        parser.onMessage(socket, "{type: ${EventType.NOTIFICATION_CHANNEL_DELETED}}")

        verifyEvent(eventsCollector[0], ChannelTruncatedEvent::class.java, EventType.CHANNEL_TRUNCATED)
        verifyEvent(
            eventsCollector[1],
            NotificationChannelTruncatedEvent::class.java,
            EventType.NOTIFICATION_CHANNEL_TRUNCATED
        )
        verifyEvent(eventsCollector[2], NotificationChannelDeletedEvent::class.java, EventType.NOTIFICATION_CHANNEL_DELETED)
    }

    private fun verifyEvent(event: ChatEvent, clazz: Class<out ChatEvent>, type: String) {
        assertThat(event).isInstanceOf(clazz)
        assertThat(event.type).isEqualTo(type)
    }
}
