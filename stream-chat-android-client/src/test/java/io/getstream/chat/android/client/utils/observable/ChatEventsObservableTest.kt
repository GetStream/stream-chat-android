package io.getstream.chat.android.client.utils.observable

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import org.junit.Before
import org.junit.Test
import java.util.Date

internal class ChatEventsObservableTest {

    private lateinit var socket: FakeChatSocket
    private lateinit var observable: ChatEventsObservable
    private lateinit var result: MutableList<ChatEvent>

    @Before
    fun before() {
        socket = FakeChatSocket()
        observable = ChatEventsObservable(socket, mock())
        result = mutableListOf()
    }

    @Test
    fun oneEventDelivery() {
        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        observable.subscribe { result.add(it) }

        socket.sendEvent(event)

        assertThat(result).isEqualTo(listOf(event))
    }

    @Test
    fun multipleEventsDelivery() {
        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val eventB = NewMessageEvent(
            EventType.MESSAGE_NEW,
            Date(),
            User(),
            "type:id",
            "type",
            "id",
            Message(),
            0,
            0,
            0
        )
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
        observable.subscribe { result.add(it) }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        assertThat(result).isEqualTo(listOf(eventA, eventB, eventC))
    }

    @Test
    fun filtering() {
        val eventA = UnknownEvent("a", Date(), null, emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), null, mapOf<Any, Any>("cid" to "myCid"))
        val eventC = UnknownEvent("c", Date(), null, emptyMap<Any, Any>())

        val filter: (ChatEvent) -> Boolean = {
            it.type == "b" && (it as? UnknownEvent)?.rawData?.get("cid") == "myCid"
        }
        observable.subscribe(filter) {
            result.add(it)
        }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)
        socket.sendEvent(eventC)

        assertThat(result).isEqualTo(listOf(eventB))
    }

    @Test
    fun unsubscription() {
        val eventA = UnknownEvent("a", Date(), null, emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), null, emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", Date(), null, emptyMap<Any, Any>())

        val subscription = observable.subscribe { result.add(it) }

        socket.sendEvent(eventA)
        socket.sendEvent(eventB)

        subscription.dispose()

        socket.sendEvent(eventC)

        assertThat(result).isEqualTo(listOf(eventA, eventB))
    }
}
