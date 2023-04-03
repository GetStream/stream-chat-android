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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

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
     * thread.
     *
     * The [callback] will only be invoked in case the [Call] is not canceled, and always on the main thread.
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
     * Awaits the result of this [Call] in a suspending way, asynchronously.
     * Safe to call from any CoroutineContext.
     *
     * Does not throw exceptions. Any errors will be wrapped in the [Result] that's returned.
     */
    public suspend fun await(): Result<T>

    /**
     * Cancels the execution of the call.
     */
    public fun cancel()

    public fun interface Callback<T : Any> {
        public fun onResult(result: Result<T>)
    }

    @InternalStreamChatApi
    public companion object {
        public fun <T : Any> callCanceledError(): Result<T> =
            Result.Failure(Error.GenericError(message = "The call was canceled before complete its execution."))

        @SuppressWarnings("TooGenericExceptionCaught")
        public suspend fun <T : Any> runCatching(
            errorMap: suspend (originalResultError: Result<T>) -> Result<T> = { it },
            block: suspend () -> Result<T>,
        ): Result<T> = try {
            block().also { yield() }
        } catch (t: Throwable) {
            errorMap(t.toResult())
        }

        private fun <T : Any> Throwable.toResult(): Result<T> = when (this) {
            is CancellationException -> callCanceledError()
            else -> Result.Failure(Error.ThrowableError(message = "", cause = this))
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
 * Wraps this [Call] into [ReturnOnErrorCall] to return an item specified by side effect function when it encounters
 * an error.
 *
 * @param scope Scope of coroutine in which to execute side effect function.
 * @param function Function that returns data in the case of error.
 */
@InternalStreamChatApi
public fun <T : Any> Call<T>.onErrorReturn(
    scope: CoroutineScope,
    function: suspend (originalError: Error) -> Result<T>,
): ReturnOnErrorCall<T> = ReturnOnErrorCall(this, scope, function)

/**
 * Shares the existing [Call] instance for the specified [identifier].
 * If no existing call found the new [Call] instance will be provided.
 */
@InternalStreamChatApi
public fun <T : Any> Call<T>.share(
    scope: CoroutineScope,
    identifier: () -> Int,
): Call<T> {
    return SharedCall(this, identifier, scope)
}

@InternalStreamChatApi
public fun Call<*>.toUnitCall(): Call<Unit> = map {}

private val onSuccessStub: (Any) -> Unit = {}
private val onErrorStub: (Error) -> Unit = {}

@InternalStreamChatApi
public fun <T : Any> Call<T>.enqueue(
    onSuccess: (T) -> Unit = onSuccessStub,
    onError: (Error) -> Unit = onErrorStub,
) {
    enqueue { result ->
        when (result) {
            is Result.Success -> onSuccess(result.value)
            is Result.Failure -> onError(result.value)
        }
    }
}
