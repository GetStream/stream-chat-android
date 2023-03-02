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
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicBoolean

internal class ZipCall<A : Any, B : Any>(
    private val callA: Call<A>,
    private val callB: Call<B>
) : Call<Pair<A, B>> {
    private val canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
        callA.cancel()
        callB.cancel()
    }

    override fun execute(): Result<Pair<A, B>> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<Pair<A, B>>) {
        callA.enqueue { resultA ->
            when {
                canceled.get() -> { /* no-op */ }
                resultA.isSuccess -> callB.enqueue { resultB ->
                    when {
                        canceled.get() -> null
                        resultB.isSuccess -> resultA.combine(resultB)
                        else -> getErrorB<A, B>(resultB)
                    }?.let(callback::onResult)
                }
                else -> callback.onResult(getErrorA<A, B>(resultA).also { callB.cancel() })
            }
        }
    }

    private fun <A : Any, B : Any> getErrorA(resultA: Result<A>): Result<Pair<A, B>> {
        return Result(resultA.error())
    }

    private fun <A : Any, B : Any> getErrorB(resultB: Result<B>): Result<Pair<A, B>> {
        return Result(resultB.error())
    }

    private fun <A : Any, B : Any> Result<A>.combine(result: Result<B>): Result<Pair<A, B>> =
        Result(Pair(this.data(), result.data()))

    override suspend fun await(): Result<Pair<A, B>> = withContext(DispatcherProvider.IO) {
        val deferredA = async { callA.await() }
        val deferredB = async { callB.await() }

        val resultA = deferredA.await()
        if (canceled.get()) return@withContext Call.callCanceledError()
        if (resultA.isError) {
            deferredB.cancel()
            return@withContext getErrorA(resultA)
        }

        val resultB = deferredB.await()
        if (canceled.get()) return@withContext Call.callCanceledError()
        if (resultB.isError) {
            return@withContext getErrorB(resultB)
        }

        resultA.combine(resultB)
    }
}
