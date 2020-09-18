package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

interface Disposable {
    val isDisposed: Boolean
    fun dispose()
}

internal interface EventSubscription : Disposable {
    fun onNext(event: ChatEvent)
}

internal open class SubscriptionImpl(
    private val filter: (ChatEvent) -> Boolean,
    listener: ((ChatEvent) -> Unit)
) : EventSubscription {

    private var listener: ((ChatEvent) -> Unit)? = listener

    override var isDisposed: Boolean = false

    override fun dispose() {
        isDisposed = true
        listener = null
    }

    final override fun onNext(event: ChatEvent) {
        check(!isDisposed) { "Subscription already disposed, onNext should not be called on it" }

        if (filter(event)) {
            try {
                listener!!.invoke(event)
            } finally {
                afterEventDelivered()
            }
        }
    }

    open fun afterEventDelivered() {}
}

internal class SingleSubscriptionImpl(
    filter: (ChatEvent) -> Boolean,
    listener: ((ChatEvent) -> Unit)
) : SubscriptionImpl(filter, listener) {

    override fun afterEventDelivered() {
        // Dispose self, so that the listener is never called again
        dispose()
    }
}
