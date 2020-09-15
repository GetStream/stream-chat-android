package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UnknownEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.Date

class TestObservable {

    lateinit var socketService: FakeSocketService
    lateinit var observable: ChatObservable
    lateinit var result: MutableList<ChatEvent>

    @Before
    fun before() {
        socketService = FakeSocketService()
        observable = ChatObservableImpl(socketService).ignoreInitState()
        result = mutableListOf()
    }

    @Test
    fun oneEventDelivery() {

        val event = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")

        observable.subscribe { result.add(it) }
        socketService.sendEvent(event)

        assertThat(result).containsSequence(event)
    }

    @Test
    fun multipleEventsDelivery() {

        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val eventB = NewMessageEvent(EventType.MESSAGE_NEW, Date(), User(), "type:id", "type", "id", Message(), 0, 0, 0)
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())

        observable.subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventA, eventB, eventC)
    }

    @Test
    fun typesFiltering() {

        val eventA = ConnectedEvent(EventType.HEALTH_CHECK, Date(), User(), "")
        val eventB = NewMessageEvent(EventType.MESSAGE_NEW, Date(), User(), "type:id", "type", "id", Message(), 0, 0, 0)
        val eventC = DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
        val eventD = ErrorEvent(EventType.CONNECTION_ERROR, Date(), ChatError("Error"))

        observable
            .filter(eventA::class.java)
            .filter(eventC::class.java)
            .subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)
        socketService.sendEvent(eventD)

        assertThat(result).containsSequence(eventA, eventC)
    }

    @Test
    fun stringTypesFiltering() {

        val eventA = UnknownEvent("a", Date(), emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", Date(), emptyMap<Any, Any>())

        observable
            .filter("a")
            .filter("b")
            .subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventA, eventB)
    }

    @Test
    fun customFiltering() {

        val eventA = UnknownEvent("a", Date(), emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), mapOf<Any, Any>("cid" to "myCid"))
        val eventC = UnknownEvent("c", Date(), emptyMap<Any, Any>())

        observable
            .filter {
                it.type == "b" && (it as? UnknownEvent)?.rawData?.get("cid") == "myCid"
            }
            .subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventB)
    }

    @Test
    fun unsubscription() {
        val eventA = UnknownEvent("a", Date(), emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", Date(), emptyMap<Any, Any>())

        val subscription = observable.subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)

        subscription.unsubscribe()

        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventA, eventB)
    }

    @Test
    fun first() {

        val eventA = UnknownEvent("a", Date(), emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", Date(), emptyMap<Any, Any>())

        observable.first().subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventA)
    }

    @Test
    fun firstAndFilter() {

        val eventA = UnknownEvent("a", Date(), emptyMap<Any, Any>())
        val eventB = UnknownEvent("b", Date(), emptyMap<Any, Any>())
        val eventC = UnknownEvent("c", Date(), emptyMap<Any, Any>())

        observable
            .first()
            .filter { it.type == "b" }
            .subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventB)
    }
}
