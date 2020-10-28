package io.getstream.chat.android.client.controllers

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.observable.Disposable
import kotlin.reflect.KClass

public inline fun <reified T : ChatEvent> ChannelController.subscribeFor(
    crossinline listener: (event: T) -> Unit
): Disposable {
    return this.subscribeFor(
        T::class.java,
        listener = { event ->
            listener(event as T)
        }
    )
}

public fun ChannelController.subscribeFor(
    vararg eventTypes: KClass<out ChatEvent>,
    listener: (event: ChatEvent) -> Unit
): Disposable {
    val javaClassTypes: Array<Class<out ChatEvent>> = eventTypes.map { it.java }.toTypedArray()
    return subscribeFor(*javaClassTypes, listener = listener)
}

public inline fun <reified T : ChatEvent> ChannelController.subscribeForSingle(
    noinline listener: (event: T) -> Unit
): Disposable {
    return this.subscribeForSingle(T::class.java, listener)
}

public fun ChannelController.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    vararg eventTypes: KClass<out ChatEvent>,
    listener: (ChatEvent) -> Unit
) {
    subscribeFor(
        lifecycleOwner = lifecycleOwner,
        eventTypes = eventTypes.map { it.java }.toTypedArray(),
        listener = listener
    )
}

public fun ChannelController.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    vararg eventTypes: Class<out ChatEvent>,
    listener: (ChatEvent) -> Unit,
) {
    subscribeFor(eventTypes = eventTypes, listener = listener).also { subscription ->
        lifecycleOwner.lifecycle.addObserver(
            object : LifecycleObserver {
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                fun onDestroy() {
                    if (subscription.isDisposed) {
                        return
                    }

                    subscription.dispose()
                }
            }
        )
    }
}
