package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener

class ChatObservableImpl(private val service: ChatSocketService) : ChatObservable {

    private val subscriptions = mutableListOf<Subscription>()
    private var eventsMapper: SocketListener = EventsMapper(this)

    fun onNext(event: ChatEvent) {
        subscriptions.forEach { it.onNext(event) }
    }

    override fun subscribe(listener: (ChatEvent) -> Unit): Subscription {
        val result = Subscription(this, listener)

        if (subscriptions.isEmpty()) {
            //subscribe to socket events only once
            service.addListener(eventsMapper)
        }

        subscriptions.add(result)
        return result
    }

    override fun unsubscribe(subscription: Subscription) {
        subscriptions.remove(subscription)

        if (subscriptions.isEmpty()) {
            service.removeListener(eventsMapper)
        }
    }

    private class EventsMapper(val observable: ChatObservableImpl) : SocketListener() {

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