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

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.Timeout
import org.mockito.kotlin.mock
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

internal class BlockedRetrofit2Call<T>(
    private val scope: CoroutineScope,
    private val value: T? = null,
    private val error: IOException? = null,
) : retrofit2.Call<T> {

    init {
        if (value == null) {
            requireNotNull(error) {
                "BlockedRetrofit2Call should be initialized with an error or value not null"
            }
        }
        if (error == null) {
            requireNotNull(value) {
                "BlockedRetrofit2Call should be initialized with an error or value not null"
            }
        }
        if (error != null && value != null) error("BlockedRetrofit2Call can't be initialized with a value and an error")
    }

    private val isBlocked = AtomicBoolean(true)
    private val started = AtomicBoolean(false)
    private val completed = AtomicBoolean(false)
    private val cancelled = AtomicBoolean(false)

    fun unblock() {
        isBlocked.set(false)
    }

    private suspend fun run() = withContext(DispatcherProvider.IO) {
        started.set(true)
        while (isBlocked.get()) {
            delay(10)
        }
        if (!cancelled.get()) completed.set(true)
    }

    fun isStarted(): Boolean = started.get()
    fun isCompleted(): Boolean = completed.get()
    override fun enqueue(callback: Callback<T>) {
        scope.launch {
            run()
            if (value != null) callback.onResponse(this@BlockedRetrofit2Call, Response.success(value))
            if (error != null) callback.onFailure(this@BlockedRetrofit2Call, error)
        }
    }

    override fun execute(): Response<T> = runBlocking {
        run()
        if (value != null) {
            Response.success(value)
        } else {
            throw error!!
        }
    }

    override fun isExecuted(): Boolean = started.get()
    override fun cancel() { cancelled.set(true) }
    override fun isCanceled(): Boolean = cancelled.get()
    override fun request(): Request = mock()
    override fun timeout(): Timeout = mock()
    override fun clone(): Call<T> = BlockedRetrofit2Call(scope, value, error)
}
