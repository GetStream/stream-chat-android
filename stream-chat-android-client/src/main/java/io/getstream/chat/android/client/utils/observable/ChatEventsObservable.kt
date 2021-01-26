package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.socket.SocketListener
import java.util.Date

internal class ChatEventsObservable(
    private val socket: ChatSocket,
    private var client: ChatClient,
) {

    private var subscriptions = setOf<EventSubscription>()
    private var eventsMapper = EventsMapper(this)
    private val logger = ChatLogger.get("ChatEventsObservable")

    private fun onNext(event: ChatEvent) {
        subscriptions.forEach { subscription ->
            if (!subscription.isDisposed) {
                subscription.onNext(event)
            }
        }
        when (event) {
            is ConnectedEvent -> {
                client.callConnectionListener(event, null)
            }
            is ErrorEvent -> {
                client.callConnectionListener(null, event.error)
            }
        }
        subscriptions = subscriptions.filterNot(Disposable::isDisposed).toSet()
        checkIfEmpty()
    }

    private fun checkIfEmpty() {
        if (subscriptions.isEmpty()) {
            socket.removeListener(eventsMapper)
        }
    }

    fun subscribe(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return addSubscription(SubscriptionImpl(filter, listener))
    }

    fun subscribeSingle(
        filter: (ChatEvent) -> Boolean = { true },
        listener: ChatEventListener<ChatEvent>,
    ): Disposable {
        return addSubscription(
            SubscriptionImpl(filter, listener).apply {
                afterEventDelivered = this::dispose
            }
        )
    }

    private fun addSubscription(subscription: EventSubscription): Disposable {
        if (subscriptions.isEmpty()) {
            // add listener to socket events only once
            socket.addListener(eventsMapper)
        }

        subscriptions = subscriptions + subscription

        return subscription
    }

    /**
     * Maps methods of [SocketListener] to events of [ChatEventsObservable]
     */
    private class EventsMapper(private val observable: ChatEventsObservable) : SocketListener() {

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
