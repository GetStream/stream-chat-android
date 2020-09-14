package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener
import java.lang.Exception
import java.util.Date

internal class ChatObservable(private val socket: ChatSocket) {

    private val subscriptions = mutableSetOf<Subscription>()
    private val disposing = mutableSetOf<Subscription>()
    private var eventsMapper = EventsMapper(this)

    private fun onNext(event: ChatEvent) {
        subscriptions.forEach { it.onNext(event) }

        subscriptions -= disposing
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        if (subscriptions.isEmpty()) {
            socket.removeListener(eventsMapper)
        }
    }

    fun subscribe(
        filter: (ChatEvent) -> Boolean = { true },
        listener: (ChatEvent) -> Unit
    ): Subscription {
        val result = SubscriptionImpl(this, listener, filter)

        if (subscriptions.isEmpty()) {
            // add listener to socket events only once
            socket.addListener(eventsMapper)
        }

        subscriptions.add(result)

        deliverInitState(result)

        return result
    }

    fun unsubscribe(subscription: Subscription) {
        try {
            subscriptions -= subscription
            checkIfEmpty()
        } catch (e: Exception) {
            disposing += subscription
        }
    }

    private fun deliverInitState(subscription: Subscription) {
        val firstEvent: ChatEvent = when (val state = socket.state) {
            is ChatSocketService.State.Connected ->
                state.event
            is ChatSocketService.State.Connecting ->
                ConnectingEvent(EventType.CONNECTION_CONNECTING, Date())
            is ChatSocketService.State.Disconnected ->
                DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date())
            else -> return
        }

        subscription.onNext(firstEvent)
    }

    /**
     * Maps methods of [SocketListener] to events of [ChatObservable]
     */
    private class EventsMapper(val observable: ChatObservable) : SocketListener() {

        override fun onConnecting() {
            observable.onNext(ConnectingEvent(EventType.CONNECTION_CONNECTING, Date()))
        }

        override fun onConnected(event: ConnectedEvent) {
            observable.onNext(event)
        }

        override fun onDisconnected() {
            observable.onNext(DisconnectedEvent(EventType.CONNECTION_DISCONNECTED, Date()))
        }

        override fun onEvent(event: ChatEvent) {
            observable.onNext(event)
        }

        override fun onError(error: ChatError) {
            observable.onNext(ErrorEvent(EventType.CONNECTION_ERROR, Date(), error))
        }
    }
}
