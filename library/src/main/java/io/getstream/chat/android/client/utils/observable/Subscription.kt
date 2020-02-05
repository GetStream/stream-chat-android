package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

open class Subscription(
    private val observable: ChatObservable,
    var listener: ((ChatEvent) -> Unit)?
) {

    open fun unsubscribe() {
        listener = null
        observable.unsubscribe(this)
    }

    fun onNext(event: ChatEvent) {
        listener?.invoke(event)
    }
}