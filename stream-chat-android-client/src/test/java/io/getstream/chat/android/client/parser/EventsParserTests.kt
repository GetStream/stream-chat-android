package io.getstream.chat.android.client.parser

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.events.ChannelTruncatedEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationChannelDeletedEvent
import io.getstream.chat.android.client.events.NotificationChannelTruncatedEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.parser2.MoshiChatParser
import io.getstream.chat.android.client.parser2.testdata.ChannelDtoTestData
import io.getstream.chat.android.client.parser2.testdata.UserDtoTestData
import io.getstream.chat.android.client.socket.EventsParser
import io.getstream.chat.android.client.utils.observable.FakeSocketService
import okhttp3.Response
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
    private val response: Response = mock()

    private val connectedEvent = """
        {
            "type": "${EventType.HEALTH_CHECK}",
            "created_at": "2020-06-10T11:04:31.000Z",
            "me" : { 
                "id": "$userId",
                "role": "",
                "banned": false,
                "online": false
            },
            "connection_id": ""
        }
    """.trimIndent()
    private val channelTruncatedEvent = """
        { 
            "type": "${EventType.CHANNEL_TRUNCATED}",
            "created_at": "2020-06-10T11:04:31.000Z",
            "cid": "",
            "channel_type": "",
            "channel_id": "",
            "user": ${UserDtoTestData.downstreamJson},
            "channel": ${ChannelDtoTestData.downstreamJson}
        }
    """.trimIndent()
    private val notificationChannelTruncatedEvent = """
        { 
            "type": "${EventType.NOTIFICATION_CHANNEL_TRUNCATED}",
            "created_at": "2020-06-10T11:04:31.000Z",
            "cid": "",
            "channel_type": "",
            "channel_id": "",
            "channel": ${ChannelDtoTestData.downstreamJson}
        }
    """.trimIndent()
    private val notificationChannelDeletedEvent = """
        { 
            "type": "${EventType.NOTIFICATION_CHANNEL_DELETED}",
            "created_at": "2020-06-10T11:04:31.000Z",
            "cid": "",
            "channel_type": "",
            "channel_id": "",
            "channel": ${ChannelDtoTestData.downstreamJson}
        }
    """.trimIndent()

    @Before
    fun before() {
        eventsCollector = mutableListOf()
        service = FakeSocketService(eventsCollector)
        parser = EventsParser(MoshiChatParser(), service)
    }

    @Test
    fun firstConnection() {
        parser.onOpen(socket, response)
        parser.onMessage(socket, connectedEvent)

        service.verifyConnectionUserId(userId)
    }

    @Test
    fun firstInvalidEvent() {
        parser.onMessage(socket, """{ "type": "$eventType" }""")

        service.verifyNoConnectionUserId()
    }

    @Test
    fun mapTypesToObjects() {
        parser.onOpen(socket, response)
        parser.onMessage(socket, connectedEvent)

        parser.onMessage(socket, channelTruncatedEvent)
        parser.onMessage(socket, notificationChannelTruncatedEvent)
        parser.onMessage(socket, notificationChannelDeletedEvent)

        verifyEvent(
            eventsCollector[0],
            ChannelTruncatedEvent::class.java,
            EventType.CHANNEL_TRUNCATED
        )
        verifyEvent(
            eventsCollector[1],
            NotificationChannelTruncatedEvent::class.java,
            EventType.NOTIFICATION_CHANNEL_TRUNCATED
        )
        verifyEvent(
            eventsCollector[2],
            NotificationChannelDeletedEvent::class.java,
            EventType.NOTIFICATION_CHANNEL_DELETED
        )
    }

    private fun verifyEvent(event: ChatEvent, clazz: Class<out ChatEvent>, type: String) {
        assertThat(event).isInstanceOf(clazz)
        assertThat(event.type).isEqualTo(type)
    }
}
