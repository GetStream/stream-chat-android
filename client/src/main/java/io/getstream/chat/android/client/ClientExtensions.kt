package io.getstream.chat.android.client

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.observable.Subscription
import kotlin.reflect.KClass

inline fun <reified T : ChatEvent> ChatClient.subscribeFor(
    crossinline listener: (event: T) -> Unit
): Subscription {
    return this.subscribeFor(
        T::class.java,
        listener = { event ->
            listener(event as T)
        }
    )
}

fun ChatClient.subscribeFor(
    vararg eventTypes: KClass<out ChatEvent>,
    listener: (event: ChatEvent) -> Unit
): Subscription {
    val javaClassTypes: Array<Class<out ChatEvent>> = eventTypes.map { it.java }.toTypedArray()
    return subscribeFor(*javaClassTypes, listener = listener)
}

inline fun <reified T : ChatEvent> ChatClient.subscribeForSingle(
    noinline listener: (event: T) -> Unit
): Subscription {
    return this.subscribeForSingle(T::class.java, listener)
}
