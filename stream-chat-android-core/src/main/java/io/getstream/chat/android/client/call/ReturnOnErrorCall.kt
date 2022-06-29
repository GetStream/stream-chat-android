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

import io.getstream.chat.android.client.call.Call.Companion.callCanceledError
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import java.util.concurrent.atomic.AtomicBoolean

/**
 * A wrapper around [Call] that swallows the error and emits new data from [onErrorReturn].
 */
public class ReturnOnErrorCall<T : Any>(
    private val originalCall: Call<T>,
    private val scope: CoroutineScope,
    private val onErrorReturn: suspend (originalError: ChatError) -> Result<T>,
) : Call<T> {

    private var job: Job? = null
    private val canceled = AtomicBoolean(false)

    override fun execute(): Result<T> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<T>) {
        scope.launch {
            originalCall.enqueue { originalResult ->
                job = scope.launch {
                    val finalResult = originalResult
                        .takeUnless { canceled.get() }
                        ?.let { map(it) }
                    yield()
                    withContext(DispatcherProvider.Main) {
                        finalResult?.let(callback::onResult)
                    }
                }
            }
        }
    }

    override fun cancel() {
        canceled.set(true)
        originalCall.cancel()
        job?.cancel()
    }

    override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
        map(
            originalCall.await()
                .takeUnless { canceled.get() }
                ?: callCanceledError()
        )
    }

    private suspend fun map(result: Result<T>): Result<T> = when (result.isSuccess) {
        true -> result
        else -> onErrorReturn(result.error())
    }
}
