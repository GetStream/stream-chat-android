package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

class JustObservable(private val event: ChatEvent) : ChatObservable {
    override fun subscribe(listener: (ChatEvent) -> Unit): Subscription {
        listener(event)
        return Subscription(this) {}
    }

    override fun unsubscribe(subscription: Subscription) {

    }
}