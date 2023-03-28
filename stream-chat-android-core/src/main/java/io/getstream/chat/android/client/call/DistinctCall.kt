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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.concurrency.SynchronizedReference
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicReference

/**
 * Reusable wrapper around [Call] which delivers a single result to all subscribers.
 */
@InternalStreamChatApi
public class DistinctCall<T : Any>(
    scope: CoroutineScope,
    private val callBuilder: () -> Call<T>,
    private val onFinished: () -> Unit,
) : Call<T> {

    private val distinctScope = scope + SupervisorJob(scope.coroutineContext.job)
    private val deferred = SynchronizedReference<Deferred<Result<T>>>()
    private val delegateCall = AtomicReference<Call<T>>()

    @InternalStreamChatApi
    public fun originCall(): Call<T> = callBuilder()

    override fun execute(): Result<T> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<T>) {
        distinctScope.launch {
            await().takeUnless { it.isCanceled }?.also { result ->
                withContext(DispatcherProvider.Main) {
                    callback.onResult(result)
                }
            }
        }
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    override suspend fun await(): Result<T> = Call.runCatching {
        deferred.getOrCreate {
            distinctScope.async {
                callBuilder()
                    .also { delegateCall.set(it) }
                    .await()
                    .also { doFinally() }
            }
        }.await()
    }

    override fun cancel() {
        delegateCall.get()?.cancel()
        distinctScope.coroutineContext.cancelChildren()
        doFinally()
    }

    private fun doFinally() {
        if (deferred.reset()) {
            onFinished()
        }
    }

    private val Result<T>.isCanceled get() = this == Call.callCanceledError<T>()
}
