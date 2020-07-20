package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

open class Subscription(
    private val observable: ChatObservable,
    private var listener: ((ChatEvent) -> Unit)?,
    private val filters: MutableList<(event: ChatEvent) -> Boolean> = mutableListOf(),
    private val firstOnly: Boolean
) {

    private var deliveredCounter = 0

    open fun unsubscribe() {
        listener = null
        filters.clear()
        observable.unsubscribe(this)
    }

    fun onNext(event: ChatEvent) {

        if (filters.isEmpty()) {
            deliver(event)
        } else {
            filters.forEach { filtered ->
                if (filtered(event)) {
                    deliver(event)
                    return
                }
            }
        }
    }

    private fun deliver(event: ChatEvent) {
        if (firstOnly) {
            if (deliveredCounter == 0) {
                deliveredCounter = 1
                listener?.invoke(event)
            }
        } else {
            deliveredCounter++
            listener?.invoke(event)
        }
    }
}
