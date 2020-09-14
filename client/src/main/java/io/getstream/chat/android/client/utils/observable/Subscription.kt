package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

interface Subscription {
    fun unsubscribe()
    fun onNext(event: ChatEvent)
}

internal class SubscriptionImpl(
    private val observable: ChatObservable,
    private var listener: ((ChatEvent) -> Unit)?,
    private val filter: (ChatEvent) -> Boolean
) : Subscription {

    override fun unsubscribe() {
        listener = null
        observable.unsubscribe(this)
    }

    override fun onNext(event: ChatEvent) {
        if (filter(event)) {
            listener?.invoke(event)
        }
    }
}
