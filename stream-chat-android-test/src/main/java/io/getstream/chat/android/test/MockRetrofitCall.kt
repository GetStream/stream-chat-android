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

package io.getstream.chat.android.test

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

public class MockRetrofitCall<T : Any>(
    public val scope: CoroutineScope,
    public val result: Result<T>,
    public val doWork: suspend () -> Unit,
) : Call<T> {

    private val logger by taggedLogger("Chat:MockRetrofitCall")

    private val executed = AtomicBoolean(false)
    private val job = AtomicReference<Job>()
    private val pendingCallback = AtomicReference<Call.Callback<T>>()

    override fun cancel() {
        logger.d { "[cancel] no args" }
        job.get()?.cancel()
        job.set(null)
        pendingCallback.get()
            ?.onResult(Result.Failure(ChatError.ThrowableError(message = "", cause = AsyncTestCallCanceledException())))
        pendingCallback.set(null)
    }

    override fun enqueue(callback: Call.Callback<T>) {
        logger.d { "[enqueue] no args" }
        if (executed.get()) {
            logger.w { "[enqueue] rejected (already executed)" }
            callback.onResult(Result.Failure(ChatError.GenericError(message = "Already executed.")))
            return
        }
        pendingCallback.set(callback)
        executed.set(true)
        job.set(
            scope.launch {
                logger.w { "[enqueue] start work" }
                doWork()
                logger.w { "[enqueue] work completed" }
                callback.onResult(result)
            }
        )
    }

    override fun execute(): Result<T> = runBlocking {
        logger.d { "[execute] no args" }
        await()
    }

    override suspend fun await(): Result<T> = suspendCancellableCoroutine { continuation ->
        logger.d { "[await] no args" }
        enqueue { result ->
            logger.v { "[await] result: $result" }
            continuation.resume(result)
        }
        continuation.invokeOnCancellation {
            logger.v { "[await] canceled: $it" }
            cancel()
        }
    }
}

public class AsyncTestCallCanceledException : CancellationException("AsyncTestCall was canceled")
