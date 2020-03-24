package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

open class Subscription(
    private val observable: ChatObservable,
    private var listener: ((ChatEvent) -> Unit)?,
    private val filters: MutableList<(event: ChatEvent) -> Boolean> = mutableListOf()
) {

    open fun unsubscribe() {
        listener = null
        filters.clear()
        observable.unsubscribe(this)
    }

    fun onNext(event: ChatEvent) {

        if (filters.isEmpty()) {
            listener?.invoke(event)
        } else {
            filters.forEach { filtered ->
                if (filtered(event)) {
                    listener?.invoke(event)
                    return
                }
            }
        }

    }
}