package io.getstream.chat.android.client.channel

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.ChatEventListener
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import kotlin.reflect.KClass

/**
 * Subscribes to events of type [T] in the channel.
 */
public inline fun <reified T : ChatEvent> ChannelClient.subscribeFor(
    listener: ChatEventListener<T>
): Disposable {
    return this.subscribeFor(
        T::class.java,
        listener = { event ->
            listener.onEvent(event as T)
        }
    )
}

/**
 * Subscribes to events of type [T] in the channel, in the lifecycle of [lifecycleOwner].
 *
 * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
 */
public inline fun <reified T : ChatEvent> ChannelClient.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    listener: ChatEventListener<T>
): Disposable {
    return this.subscribeFor(
        lifecycleOwner,
        T::class.java,
        listener = { event ->
            listener.onEvent(event as T)
        }
    )
}

/**
 * Subscribes to the specific [eventTypes] of the channel.
 */
public fun ChannelClient.subscribeFor(
    vararg eventTypes: KClass<out ChatEvent>,
    listener: ChatEventListener<ChatEvent>
): Disposable {
    val javaClassTypes: Array<Class<out ChatEvent>> = eventTypes.map { it.java }.toTypedArray()
    return subscribeFor(*javaClassTypes, listener = listener)
}

/**
 * Subscribes to the specific [eventTypes] of the channel, in the lifecycle of [lifecycleOwner].
 *
 * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
 */
public fun ChannelClient.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    vararg eventTypes: KClass<out ChatEvent>,
    listener: ChatEventListener<ChatEvent>
): Disposable {
    val javaClassTypes: Array<Class<out ChatEvent>> = eventTypes.map { it.java }.toTypedArray()
    return subscribeFor(lifecycleOwner, *javaClassTypes, listener = listener)
}

/**
 * Subscribes for the next channel event of type [T].
 */
public inline fun <reified T : ChatEvent> ChannelClient.subscribeForSingle(
    listener: ChatEventListener<T>
): Disposable {
    return this.subscribeForSingle(T::class.java, listener)
}
