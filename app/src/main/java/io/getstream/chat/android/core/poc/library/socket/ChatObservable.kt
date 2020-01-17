package io.getstream.chat.android.core.poc.library.socket

import io.getstream.chat.android.core.poc.library.Event

class ChatObservable(private val service: StreamWebSocketService) {

    private val subscriptions = mutableListOf<Subscription>()
    private var wsListener: WsListener? = null

    fun onNext(event: Event) {
        subscriptions.forEach { it.onNext(event) }
    }

    fun subscribe(listener: (Event) -> Unit): Subscription {
        val result = Subscription(this, listener)
        subscriptions.add(result)

        if (wsListener == null) {
            wsListener = object :
                WsListener {
                override fun onWSEvent(event: Event) {
                    onNext(event)
                }
            }
            service.addSocketListener(wsListener!!)
        }

        return result
    }

    fun unsubscribe(subscription: Subscription) {
        subscriptions.remove(subscription)

        if (subscriptions.isEmpty()) {
            service.removeSocketListener(wsListener!!)
            wsListener = null
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
}