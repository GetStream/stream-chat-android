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

import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicReference

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
internal class DistinctCall<T : Any>(
    scope: CoroutineScope,
    private val timeoutInMillis: Long = TIMEOUT,
    private val callBuilder: () -> Call<T>,
    private val onFinished: () -> Unit,
) : Call<T> {

    private val distinctScope = scope + SupervisorJob(scope.coroutineContext.job)
    private val deferredRef = AtomicReference<Deferred<Result<T>>>()

    internal fun originCall(): Call<T> = callBuilder()

    override fun execute(): Result<T> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<T>) {
        distinctScope.launch {
            await().takeUnless { it.isCanceled }?.also { result ->
                callback.onResult(result)
            }
        }
    }

    override suspend fun await(): Result<T> = try {
        val deferred = deferredRef.get() ?: callBuilder().awaitAsync().also {
            deferredRef.set(it)
        }
        deferred.await()
    } catch (e: Throwable) {
        Result.error(e.mapCancellation())
    } finally {
        doFinally()
    }

    override fun cancel() {
        try {
            distinctScope.coroutineContext.cancelChildren()
        } finally {
            doFinally()
        }
    }

    private fun doFinally() {
        deferredRef.set(null)
        onFinished()
    }

    private fun Call<T>.awaitAsync(): Deferred<Result<T>> = distinctScope.async {
        withTimeout(timeoutInMillis) {
            await()
        }
    }

    private fun Throwable.mapCancellation(): Throwable = when (this) {
        is TimeoutCancellationException -> CallTimeoutException()
        is CancellationException -> CallCanceledException()
        else -> this
    }

    private val Result<T>.isCanceled get() = isError && error().cause is CallCanceledException

    private companion object {
        private const val TIMEOUT = 60_000L
    }
}
