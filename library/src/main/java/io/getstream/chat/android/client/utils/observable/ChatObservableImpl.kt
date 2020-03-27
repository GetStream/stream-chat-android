package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.*
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener

internal class ChatObservableImpl(private val service: ChatSocketService) : ChatObservable {

    private val subscriptions = mutableListOf<Subscription>()
    private var eventsMapper = EventsMapper(this)
    private val filters = mutableListOf<(event: ChatEvent) -> Boolean>()
    private var first = false
    private var ignoreInitState = false

    fun onNext(event: ChatEvent) {
        subscriptions.forEach { it.onNext(event) }
    }

    override fun filter(eventType: String): ChatObservable {
        return filter { it.type == eventType }
    }

    override fun filter(predicate: (event: ChatEvent) -> Boolean): ChatObservable {
        filters.add(predicate)
        return this
    }

    override fun filter(vararg types: Class<out ChatEvent>): ChatObservable {
        return filter { event ->
            types.any { type ->
                type.isInstance(event)
            }
        }
    }

    override fun first(): ChatObservable {
        first = true
        return this
    }

    override fun ignoreInitState(): ChatObservable {
        this.ignoreInitState = true
        return this
    }

    override fun subscribe(listener: (ChatEvent) -> Unit): Subscription {
        val result = Subscription(this, listener, filters, first)

        if (subscriptions.isEmpty()) {
            // add listener to socket events only once
            service.addListener(eventsMapper)
        }

        subscriptions.add(result)

        if (!ignoreInitState) deliverInitState(result)

        return result
    }

    override fun unsubscribe(subscription: Subscription) {
        subscriptions.remove(subscription)

        if (subscriptions.isEmpty()) {
            service.removeListener(eventsMapper)
        }
    }

    private fun deliverInitState(subscription: Subscription) {

        var firstEvent: ChatEvent? = null

        when (val state = service.state) {
            is ChatSocketService.State.Connected -> firstEvent = state.event
            is ChatSocketService.State.Connecting -> firstEvent = ConnectingEvent()
            is ChatSocketService.State.Disconnected -> firstEvent = DisconnectedEvent()
        }

        if (firstEvent != null) subscription.onNext(firstEvent)
    }

    private class EventsMapper(val observable: ChatObservableImpl) : SocketListener() {

        val connectingEvent = ConnectingEvent()
        val disconnectedEvent = DisconnectedEvent()
        var connectedEvent: ConnectedEvent? = null

        override fun onConnecting() {
            observable.onNext(connectingEvent)
        }

        override fun onConnected(event: ConnectedEvent) {
            connectedEvent = event
            observable.onNext(event)
        }

        override fun onDisconnected() {
            observable.onNext(disconnectedEvent)
        }

        override fun onEvent(event: ChatEvent) {
            observable.onNext(event)
        }

        override fun onError(error: ChatError) {
            observable.onNext(ErrorEvent(error))
        }
    }
}