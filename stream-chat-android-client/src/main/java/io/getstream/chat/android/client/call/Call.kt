package io.getstream.chat.android.client.call

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result
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
    @UiThread
    public fun enqueue(callback: (Result<T>) -> Unit = {})

    /**
     * Cancels the execution of the call, if cancellation is supported for the operation.
     *
     * Note that calls can not be cancelled when running them with [execute].
     */
    public fun cancel()
}

/**
 * Awaits the result of [this] Call in a suspending way, asynchronously.
 * Safe to call from any CoroutineContext.
 *
 * Does not throw exceptions. Any errors will be wrapped in the [Result] that's returned.
 */
public suspend fun <T : Any> Call<T>.await(): Result<T> = suspendCancellableCoroutine { continuation ->
    this.enqueue { result ->
        continuation.resume(result)
    }

    continuation.invokeOnCancellation {
        this.cancel()
    }
}

internal fun <T : Any, K : Any> Call<T>.map(mapper: (T) -> K): Call<K> {
    return MapCall(this, mapper)
}

internal fun <T : Any, K : Any> Call<T>.zipWith(call: Call<K>): Call<Pair<T, K>> {
    return ZipCall.zip(this, call)
}
