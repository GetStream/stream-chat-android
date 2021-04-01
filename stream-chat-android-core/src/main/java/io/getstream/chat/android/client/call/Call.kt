package io.getstream.chat.android.client.call

import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * A pending operation waiting to be ran.
 *
 * There are several ways to run a [Call]:
 * - [execute]: synchronous, blocking
 * - [enqueue]: async, callback based
 * - [await]: async, suspending call
 *
 * Running a [Call] more than once results in undefined behaviour.
 */
public interface Call<T : Any> {
    /**
     * Executes the call synchronously, in a blocking way. Only call this from a background thread.
     */
    @WorkerThread
    public fun execute(): Result<T>

    /**
     * Executes the call asynchronously, on a background thread. Safe to call from the main
     * thread. The [callback] will always be invoked on the main thread.
     */
    public fun enqueue(callback: Callback<T>)

    /**
     * Executes the call asynchronously, on a background thread. Safe to call from the main
     * thread.
     *
     * To get notified of the result and handle errors, use enqueue(callback) instead.
     */
    public fun enqueue(): Unit = enqueue {}

    /**
     * Cancels the execution of the call, if cancellation is supported for the operation.
     *
     * Note that calls can not be cancelled when running them with [execute].
     */
    public fun cancel()

    public fun interface Callback<T : Any> {
        public fun onResult(result: Result<T>)
    }
}

/**
 * Awaits the result of [this] Call in a suspending way, asynchronously.
 * Safe to call from any CoroutineContext.
 *
 * Does not throw exceptions. Any errors will be wrapped in the [Result] that's returned.
 */
public suspend fun <T : Any> Call<T>.await(): Result<T> {
    if (this is CoroutineCall<T>) {
        return this.awaitImpl()
    }
    return suspendCancellableCoroutine { continuation ->
        this.enqueue { result ->
            continuation.resume(result)
        }

        continuation.invokeOnCancellation {
            this.cancel()
        }
    }
}

@InternalStreamChatApi
public fun <T : Any, K : Any> Call<T>.map(mapper: (T) -> K): Call<K> {
    return MapCall(this, mapper)
}

@InternalStreamChatApi
public fun <T : Any, K : Any> Call<T>.zipWith(call: Call<K>): Call<Pair<T, K>> {
    return ZipCall(this, call)
}

@InternalStreamChatApi
public fun Call<*>.toUnitCall(): Call<Unit> = map {}
