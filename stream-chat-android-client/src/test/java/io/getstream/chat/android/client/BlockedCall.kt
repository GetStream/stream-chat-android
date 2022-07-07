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
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

public class BlockedCall<T : Any>(private val result: Result<T>) : Call<T> {

    private val isBlocked = AtomicBoolean(true)
    private val started = AtomicBoolean(false)
    private val completed = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    public fun unblock() {
        isBlocked.set(false)
    }

    private suspend fun getResult() = withContext(DispatcherProvider.IO) {
        started.set(true)
        while (isBlocked.get()) {
            delay(10)
        }

        if (!cancelled.get()) {
            completed.set(true)
        }

        result
    }

    public fun isStarted(): Boolean = started.get()
    public fun isCompleted(): Boolean = completed.get()
    public fun isCanceled(): Boolean = cancelled.get()

    override fun execute(): Result<T> = runBlocking { getResult() }
    override suspend fun await(): Result<T> = withContext(DispatcherProvider.IO) { getResult() }

    override fun enqueue(callback: Call.Callback<T>) {
        CoroutineScope(DispatcherProvider.IO).launch {
            callback.onResult(getResult())
        }
    }

    override fun cancel() {
        cancelled.set(true)
    }
}
