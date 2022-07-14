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

    private val executed = AtomicBoolean(false)
    private val job = AtomicReference<Job>()
    private val pendingCallback = AtomicReference<Call.Callback<T>>()

    override fun cancel() {
        job.get()?.cancel()
        job.set(null)
        pendingCallback.get()?.onResult(Result.error(AsyncTestCallCanceledException()))
        pendingCallback.set(null)
    }

    override fun enqueue(callback: Call.Callback<T>) {
        if (executed.get()) {
            callback.onResult(Result.error(ChatError(message = "Already executed.")))
            return
        }
        pendingCallback.set(callback)
        executed.set(true)
        job.set(
            scope.launch {
                doWork()
                callback.onResult(result)
            }
        )
    }

    override fun execute(): Result<T> = runBlocking { await() }

    override suspend fun await(): Result<T> = suspendCancellableCoroutine { continuation ->
        enqueue { result ->
            continuation.resume(result)
        }
        continuation.invokeOnCancellation {
            cancel()
        }
    }
}

public class AsyncTestCallCanceledException : CancellationException("AsyncTestCall was canceled")
