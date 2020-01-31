package io.getstream.chat.android.client.socket

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*

class ChatObservable(private val service: ChatSocketService) {

    private val subscriptions = mutableListOf<Subscription>()
    private var wsListener: SocketListener = EventsMapper(this)
    private var subscirbed = false

    fun onNext(event: ChatEvent) {
        //TODO: deliver connection event if it exists
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

        override fun onConnecting() {
            observable.onNext(ConnectingEvent())
        }

        override fun onEvent(event: ChatEvent) {
            observable.onNext(event)
        }

        override fun onConnected(event: ConnectedEvent) {
            observable.onNext(event)
        }

        override fun onDisconnected() {
            observable.onNext(DisconnectedEvent())
        }

        override fun onError(error: ChatError) {
            observable.onNext(ErrorEvent(error))
        }
    }
}