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
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Callback
import retrofit2.Response
import retrofit2.awaitResponse
import java.util.concurrent.atomic.AtomicBoolean

internal class RetrofitCall<T : Any>(
    private val call: retrofit2.Call<T>,
    private val parser: ChatParser,
    private val scope: CoroutineScope,
) : Call<T> {

    private var canceled = AtomicBoolean(false)

    override fun cancel() {
        canceled.set(true)
        call.cancel()
    }

    override fun execute(): Result<T> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<T>) {
        scope.launch {
            enqueue(call) { result ->
                scope.launch { notifyResult(result) { callback.onResult(it) } }
            }
        }
    }

    private suspend fun enqueue(call: retrofit2.Call<T>, callback: (Result<T>) -> Unit) =
        withContext(scope.coroutineContext) {
            call.enqueue(
                object : Callback<T> {
                    override fun onResponse(call: retrofit2.Call<T>, response: Response<T>) {
                        scope.launch {
                            response
                                .takeUnless { canceled.get() }
                                ?.getResult()
                                ?.let { notifyResult(it, callback) }
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<T>, t: Throwable) {
                        scope.launch {
                            t.takeUnless { canceled.get() }?.toFailedResult()?.let { notifyResult(it, callback) }
                        }
                    }
                }
            )
        }

    override suspend fun await(): Result<T> = withContext(scope.coroutineContext) {
        call.getResult().takeUnless { canceled.get() } ?: callCanceledError()
    }

    private suspend fun notifyResult(result: Result<T>, callback: (Result<T>) -> Unit) =
        withContext(DispatcherProvider.Main) {
            result.takeUnless { canceled.get() }?.let(callback)
        }

    private fun Throwable.toFailedResult(): Result<T> = Result(this.toFailedError())

    private fun Throwable.toFailedError(): ChatError = when (this) {
        is ChatRequestError -> ChatNetworkError.create(streamCode, message.toString(), statusCode, cause)
        else -> ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED, this)
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun retrofit2.Call<T>.getResult(): Result<T> = withContext(scope.coroutineContext) {
        try {
            awaitResponse().getResult()
        } catch (t: Throwable) {
            t.toFailedResult()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun Response<T>.getResult(): Result<T> = withContext(scope.coroutineContext) {
        if (isSuccessful) {
            try {
                Result(body()!!)
            } catch (t: Throwable) {
                t.toFailedResult()
            }
        } else {
            val errorBody = errorBody()

            if (errorBody != null) {
                Result(parser.toError(errorBody))
            } else {
                Result(parser.toError(raw()))
            }
        }
    }
}
