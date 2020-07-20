package io.getstream.chat.android.client.utils.observable

import io.getstream.chat.android.client.events.ChatEvent

interface ChatObservable {
    fun filter(eventType: String): ChatObservable
    fun filter(predicate: (event: ChatEvent) -> Boolean): ChatObservable
    fun filter(vararg types: Class<out ChatEvent>): ChatObservable
    fun first(): ChatObservable
    fun subscribe(listener: (ChatEvent) -> Unit): Subscription
    fun unsubscribe(subscription: Subscription)
    fun ignoreInitState(): ChatObservable
}
