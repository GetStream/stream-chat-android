package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.ConnectingEvent
import io.getstream.chat.android.client.events.DisconnectedEvent
import io.getstream.chat.android.client.events.ErrorEvent
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.socket.ChatSocketService
import io.getstream.chat.android.client.socket.SocketListener
import java.util.Date

@Deprecated("Use the subscribe methods on ChatClient or ChannelController directly instead")
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

    override fun subscribe(listener: ChatClient.ChatEventListener<ChatEvent>): Subscription {
        val result = Subscription(this, listener, filters, first)

        if (subscriptions.isEmpty()) {
            // add listener to socket events only once
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

    /**
     * Maps methods of [SocketListener] to events of [ChatObservable]
     */
    private class EventsMapper(val observable: ChatObservableImpl) : SocketListener() {

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
