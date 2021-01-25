package io.getstream.chat.android.client

import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.token.TokenProvider
import io.getstream.chat.android.client.utils.observable.Disposable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

/**
 * Subscribes to client events of type [T].
 */
public inline fun <reified T : ChatEvent> ChatClient.subscribeFor(
    listener: ChatClient.ChatEventListener<T>,
): Disposable {
    return this.subscribeFor(
        T::class.java,
        listener = { event -> listener.onEvent(event as T) }
    )
}

/**
 * Subscribes to client events of type [T], in the lifecycle of [lifecycleOwner].
 *
 * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
 */
public inline fun <reified T : ChatEvent> ChatClient.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    listener: ChatClient.ChatEventListener<T>,
): Disposable {
    return this.subscribeFor(
        lifecycleOwner,
        T::class.java,
        listener = { event -> listener.onEvent(event as T) }
    )
}

/**
 * Subscribes to the specific [eventTypes] of the client.
 */
public fun ChatClient.subscribeFor(
    vararg eventTypes: KClass<out ChatEvent>,
    listener: ChatClient.ChatEventListener<ChatEvent>,
): Disposable {
    val javaClassTypes: Array<Class<out ChatEvent>> = eventTypes.map { it.java }.toTypedArray()
    return subscribeFor(*javaClassTypes, listener = listener)
}

/**
 * Subscribes to the specific [eventTypes] of the client, in the lifecycle of [lifecycleOwner].
 *
 * Only receives events when the lifecycle is in a STARTED state, otherwise events are dropped.
 */
public fun ChatClient.subscribeFor(
    lifecycleOwner: LifecycleOwner,
    vararg eventTypes: KClass<out ChatEvent>,
    listener: ChatClient.ChatEventListener<ChatEvent>,
): Disposable {
    val javaClassTypes: Array<Class<out ChatEvent>> = eventTypes.map { it.java }.toTypedArray()
    return subscribeFor(lifecycleOwner, *javaClassTypes, listener = listener)
}

/**
 * Subscribes for the next client event of type [T].
 */
public inline fun <reified T : ChatEvent> ChatClient.subscribeForSingle(
    listener: ChatClient.ChatEventListener<T>,
): Disposable {
    return this.subscribeForSingle(T::class.java, listener)
}

/**
 * Runs [ChatClient.setUser] in a suspending way.
 * Throws exceptions if errors occur during the call.
 */
public suspend fun ChatClient.setUserAndAwait(
    user: User,
    token: String,
): InitConnectionListener.ConnectionData = suspendCoroutine { cont ->
    setUser(user, token, initConnectionListener(cont))
}

/**
 * Runs [ChatClient.setUser] in a suspending way.
 * Throws exceptions if errors occur during the call.
 */
public suspend fun ChatClient.setUserAndAwait(
    user: User,
    tokenProvider: TokenProvider,
): InitConnectionListener.ConnectionData = suspendCoroutine { cont ->
    setUser(user, tokenProvider, initConnectionListener(cont))
}

private fun initConnectionListener(cont: Continuation<InitConnectionListener.ConnectionData>) =
    object : InitConnectionListener() {
        override fun onSuccess(data: ConnectionData) {
            cont.resume(data)
        }

        override fun onError(error: ChatError) {
            cont.resumeWithException(error.cause ?: RuntimeException(error.message))
        }
    }
