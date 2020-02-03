package io.getstream.chat.android.client.observable

import io.getstream.chat.android.client.events.ChatEvent

interface ChatObservable {
    fun subscribe(listener: (ChatEvent) -> Unit): Subscription
    fun unsubscribe(subscription: Subscription)
}