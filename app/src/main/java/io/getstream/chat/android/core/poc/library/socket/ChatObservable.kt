package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.Event
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.errors.ChatError

class ChatObservable(private val service: StreamWebSocketService) {

    private val subscriptions = mutableListOf<Subscription>()
    private var wsListener: SocketListener = EventsMapper(this)
    private var subscirbed = false

    fun onNext(event: Event) {
        subscriptions.forEach { it.onNext(event) }
    }

    fun subscribe(listener: (Event) -> Unit): Subscription {
        val result = Subscription(this, listener)
        subscriptions.add(result)

        if (!subscirbed) {
            subscirbed = true
            service.addSocketListener(wsListener)
        }

        return result
    }

    fun unsubscribe(subscription: Subscription) {
        subscriptions.remove(subscription)

        if (subscriptions.isEmpty()) {
            service.removeSocketListener(wsListener)
        }
    }

    class Subscription(
        private val observable: ChatObservable,
        private var listener: ((Event) -> Unit)?
    ) {

        fun unsubscribe() {
            listener = null
            observable.unsubscribe(this)
        }

        fun onNext(event: Event) {
            listener?.invoke(event)
        }
    }

    private class EventsMapper(val observable: ChatObservable) : SocketListener() {

        override fun onSocketOpen() {
            observable.onNext(Event(EventType.CONNECTION_SOCKET_OPEN))
        }

        override fun onSocketClosing(code: Int, reason: String) {
            observable.onNext(Event(EventType.CONNECTION_SOCKET_CLOSING))
        }

        override fun onSocketClosed(code: Int, reason: String) {
            observable.onNext(Event(EventType.CONNECTION_SOCKET_CLOSED))
        }

        override fun onRemoteEvent(event: Event) {
            observable.onNext(event)
        }

        override fun onSocketFailure(error: ChatError) {
            observable.onNext(Event(EventType.CONNECTION_SOCKET_FAILURE))
        }

        override fun connectionResolved(connection: ConnectionData) {
            observable.onNext(Event(EventType.CONNECTION_RESOLVED))
        }

        override fun connectionRecovered(connection: ConnectionData) {
            observable.onNext(Event(EventType.CONNECTION_RECOVERED))
        }

        override fun onDisconnectCalled() {
            observable.onNext(Event(EventType.CONNECTION_DISCONNECT))
        }

        override fun tokenExpired() {
            observable.onNext(Event(EventType.TOKEN_EXPIRED))
        }

        override fun onError(error: ChatError) {
            observable.onNext(Event(EventType.CONNECTION_ERROR))
        }
    }
}