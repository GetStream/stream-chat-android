package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.observable.ChatObservable
import io.getstream.chat.android.client.utils.observable.Subscription

class JustObservable(private val event: ChatEvent) : ChatObservable {

    override fun filter(eventType: String): ChatObservable {
        return this
    }

    override fun filter(predicate: (event: ChatEvent) -> Boolean): ChatObservable {
        return this
    }

    override fun filter(vararg types: Class<out ChatEvent>): ChatObservable {
        return this
    }

    override fun first(): ChatObservable {
        return this
    }

    override fun subscribe(listener: (ChatEvent) -> Unit): Subscription {
        listener(event)
        return Subscription(this, listener, mutableListOf(), false)
    }

    override fun unsubscribe(subscription: Subscription) {
    }

    override fun ignoreInitState(): ChatObservable {
        return this
    }
}
