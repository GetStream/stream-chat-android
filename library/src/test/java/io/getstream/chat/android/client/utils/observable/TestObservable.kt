package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TestObservable {

    lateinit var socketService: FakeSocketService
    lateinit var observable: ChatObservable
    lateinit var result: MutableList<ChatEvent>

    @Before
    fun before() {
        socketService = FakeSocketService()
        observable = ChatObservableImpl(socketService)
        result = mutableListOf()
    }

    @Test
    fun oneEventDelivery() {

        val event = ConnectedEvent()

        observable.subscribe { result.add(it) }
        socketService.sendEvent(event)

        assertThat(result).containsSequence(event)
    }

    @Test
    fun multipleEventsDelivery() {

        val eventA = ConnectedEvent()
        val eventB = NewMessageEvent()
        val eventC = DisconnectedEvent()

        observable.subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventA, eventB, eventC)
    }

    @Test
    fun typesFiltering() {

        val eventA = ConnectedEvent()
        val eventB = NewMessageEvent()
        val eventC = DisconnectedEvent()
        val eventD = ErrorEvent(ChatError("error"))

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

        val eventA = ChatEvent("a")
        val eventB = ChatEvent("b")
        val eventC = ChatEvent("c")

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

        val eventA = ChatEvent("a")
        val eventB = ChatEvent("b")
        val eventC = ChatEvent("c")

        eventB.cid = "cid"

        observable
            .filter {
                it.type == "b" && it.cid == "cid"
            }
            .subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)
        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventB)
    }

    @Test
    fun unsubscription() {
        val eventA = ChatEvent("a")
        val eventB = ChatEvent("b")
        val eventC = ChatEvent("c")

        val subscription = observable.subscribe { result.add(it) }

        socketService.sendEvent(eventA)
        socketService.sendEvent(eventB)

        subscription.unsubscribe()

        socketService.sendEvent(eventC)

        assertThat(result).containsSequence(eventA, eventB)
    }


}