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
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.log.taggedLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

@InternalStreamChatApi
public class CoroutineCall<T : Any>(
    private val scope: CoroutineScope,
    private val suspendingTask: suspend CoroutineScope.() -> Result<T>,
) : Call<T> {

    private val logger by taggedLogger("Chat:CoroutineCall")

    private val jobs = hashSetOf<Job>()

    override fun execute(): Result<T> = runBlocking { await() }

    override suspend fun await(): Result<T> = Call.runCatching {
        logger.d { "[await] no args" }
        withContext(scope.coroutineContext) {
            jobs.addFrom(coroutineContext)
            suspendingTask()
        }
    }

    override fun cancel() {
        logger.d { "[cancel] no args" }
        jobs.cancelAll()
    }

    override fun enqueue(callback: Call.Callback<T>) {
        logger.d { "[enqueue] no args" }
        scope.launch {
            jobs.addFrom(coroutineContext)
            val result = suspendingTask()
            withContext(DispatcherProvider.Main) {
                callback.onResult(result)
            }
        }
    }

    private fun HashSet<Job>.cancelAll() {
        synchronized(this) {
            forEach {
                it.cancel()
            }
            clear()
        }
    }

    private fun HashSet<Job>.addFrom(context: CoroutineContext) {
        synchronized(this) {
            context[Job]?.also {
                add(it)
            }
        }
    }
}
