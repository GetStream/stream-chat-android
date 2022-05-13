/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.call

import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
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
     * Clones the current call. Calls can't be used more than once,  use this if you need to use the same call for
     * many requests.
     */
    public fun clone(): Call<T>

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

/**
 * Runs a call using coroutines scope
 */
@InternalStreamChatApi
public fun <T : Any> Call<T>.launch(scope: CoroutineScope) {
    scope.launch {
        this@launch.await()
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
public fun <T : Any> Call<T>.doOnStart(scope: CoroutineScope, function: suspend () -> Unit): Call<T> =
    DoOnStartCall(this, scope, function)

@InternalStreamChatApi
public fun <T : Any> Call<T>.doOnResult(scope: CoroutineScope, function: suspend (Result<T>) -> Unit): Call<T> =
    DoOnResultCall(this, scope, function)

@InternalStreamChatApi
public fun <T : Any> Call<T>.withPrecondition(
    scope: CoroutineScope,
    precondition: suspend () -> Result<Unit>,
): Call<T> =
    WithPreconditionCall(this, scope, precondition)

/**
 * Wraps this [Call] into [ReturnOnErrorCall] to return an item specified by side effect function when it encounters an error.
 *
 * @param scope Scope of coroutine in which to execute side effect function.
 * @param onError Function that returns data in the case of error.
 */
@InternalStreamChatApi
public fun <T : Any> Call<T>.onErrorReturn(
    scope: CoroutineScope,
    function: suspend (originalError: ChatError) -> Result<T>,
): ReturnOnErrorCall<T> = ReturnOnErrorCall(this, scope, function)

@InternalStreamChatApi
public fun Call<*>.toUnitCall(): Call<Unit> = map {}

private val onSuccessStub: (Any) -> Unit = {}
private val onErrorStub: (ChatError) -> Unit = {}

@InternalStreamChatApi
public fun <T : Any> Call<T>.enqueue(
    onSuccess: (T) -> Unit = onSuccessStub,
    onError: (ChatError) -> Unit = onErrorStub,
) {
    enqueue {
        if (it.isSuccess) {
            onSuccess(it.data())
        } else {
            onError(it.error())
        }
    }
}
