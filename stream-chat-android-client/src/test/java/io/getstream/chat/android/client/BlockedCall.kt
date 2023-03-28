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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.Result
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

internal class BlockedCall<T : Any>(private val result: Result<T>) : Call<T> {

    private val isBlocked = AtomicBoolean(true)
    private val started = AtomicBoolean(false)
    private val completed = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    fun unblock() {
        isBlocked.set(false)
    }

    fun block() {
        isBlocked.set(true)
    }

    private suspend fun awaitResult() = withContext(DispatcherProvider.IO) {
        try {
            started.set(true)
            while (isBlocked.get()) {
                delay(10)
            }
            if (!cancelled.get()) {
                completed.set(true)
            }
            result
        } catch (e: Throwable) {
            if (e is CancellationException) {
                cancelled.set(true)
            }
            throw e
        }
    }

    fun isStarted(): Boolean = started.get()
    fun isCompleted(): Boolean = completed.get()
    fun isCanceled(): Boolean = cancelled.get()

    override fun execute(): Result<T> = runBlocking { awaitResult() }
    override suspend fun await(): Result<T> = withContext(DispatcherProvider.IO) { awaitResult() }

    override fun enqueue(callback: Call.Callback<T>) {
        CoroutineScope(DispatcherProvider.IO).launch {
            callback.onResult(awaitResult())
        }
    }

    override fun cancel() {
        cancelled.set(true)
    }

    fun uncancel() {
        cancelled.set(false)
    }
}
