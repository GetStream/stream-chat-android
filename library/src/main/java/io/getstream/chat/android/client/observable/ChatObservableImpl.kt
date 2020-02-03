package io.getstream.chat.android.client.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.ChatSocketServiceImpl
import io.getstream.chat.android.client.socket.SocketListener

class ChatObservableImpl(private val service: ChatSocketService) :
    ChatObservable {

    private val subscriptions = mutableListOf<Subscription>()
    private var listener: SocketListener =
        EventsMapper(
            this
        )
    private var subscirbed = false

    fun onNext(event: ChatEvent) {
        //TODO: deliver connection event if it exists
        subscriptions.forEach { it.onNext(event) }
    }

    override fun subscribe(listener: (ChatEvent) -> Unit): Subscription {
        val result =
            Subscription(
                this,
                listener
            )
        subscriptions.add(result)

        if (!subscirbed) {
            subscirbed = true
            service.addListener(this.listener)
        }

        return result
    }

    override fun unsubscribe(subscription: Subscription) {
        subscriptions.remove(subscription)

        if (subscriptions.isEmpty()) {
            service.removeListener(listener)
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