package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.events.ChatEvent
import io.getstream.chat.android.core.poc.library.EventType
import io.getstream.chat.android.core.poc.library.errors.ChatError
import io.getstream.chat.android.core.poc.library.events.ConnectionEvent
import io.getstream.chat.android.core.poc.library.events.LocalEvent

class ChatObservable(private val service: StreamWebSocketService) {

    private val subscriptions = mutableListOf<Subscription>()
    private var wsListener: SocketListener = EventsMapper(this)
    private var subscirbed = false

    fun onNext(event: ChatEvent) {
        subscriptions.forEach { it.onNext(event) }
    }

    fun subscribe(listener: (ChatEvent) -> Unit): Subscription {
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
        private var listener: ((ChatEvent) -> Unit)?
    ) {

        fun unsubscribe() {
            listener = null
            observable.unsubscribe(this)
        }

        fun onNext(event: ChatEvent) {
            listener?.invoke(event)
        }
    }

    private class EventsMapper(val observable: ChatObservable) : SocketListener() {

        override fun onSocketOpen() {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_SOCKET_OPEN
                )
            )
        }

        override fun onSocketClosing(code: Int, reason: String) {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_SOCKET_CLOSING
                )
            )
        }

        override fun onSocketClosed(code: Int, reason: String) {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_SOCKET_CLOSED
                )
            )
        }

        override fun onRemoteEvent(event: ChatEvent) {
            observable.onNext(event)
        }

        override fun onSocketFailure(error: ChatError) {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_SOCKET_FAILURE
                )
            )
        }

        override fun connectionResolved(event: ConnectionEvent) {
            observable.onNext(event)
        }

        override fun connectionRecovered(connection: ConnectionData) {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_RECOVERED
                )
            )
        }

        override fun onDisconnectCalled() {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_DISCONNECT
                )
            )
        }

        override fun tokenExpired() {
            observable.onNext(
                LocalEvent(
                    EventType.TOKEN_EXPIRED
                )
            )
        }

        override fun onError(error: ChatError) {
            observable.onNext(
                LocalEvent(
                    EventType.CONNECTION_ERROR
                )
            )
        }
    }
}