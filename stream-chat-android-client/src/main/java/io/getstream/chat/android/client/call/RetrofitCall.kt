/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.errors.fromChatErrorCode
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Response
import retrofit2.awaitResponse

internal class RetrofitCall<T : Any>(
    private val call: retrofit2.Call<T>,
    private val parser: ChatParser,
    scope: CoroutineScope,
) : Call<T> {
    private val callScope = scope + SupervisorJob(scope.coroutineContext.job)

    override fun cancel() {
        call.cancel()
        callScope.coroutineContext.cancelChildren()
    }

    override fun execute(): Result<T> = runBlocking { await() }

    override fun enqueue(callback: Call.Callback<T>) {
        callScope.launch { notifyResult(call.getResult(), callback) }
    }

    override suspend fun await(): Result<T> = Call.runCatching {
        withContext(callScope.coroutineContext) {
            call.getResult()
        }
    }

    private suspend fun notifyResult(result: Result<T>, callback: Call.Callback<T>) =
        withContext(DispatcherProvider.Main) {
            callback.onResult(result)
        }

    private fun Throwable.toFailedResult(): Result<T> = Result.Failure(this.toFailedError())

    private fun Throwable.toFailedError(): Error = when (this) {
        is ChatRequestError -> Error.NetworkError(
            serverErrorCode = streamCode,
            message = message.toString(),
            statusCode = statusCode,
            cause = cause,
        )
        else -> Error.NetworkError.fromChatErrorCode(
            chatErrorCode = ChatErrorCode.NETWORK_FAILED,
            cause = this,
        )
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun retrofit2.Call<T>.getResult(): Result<T> = withContext(callScope.coroutineContext) {
        try {
            awaitResponse().getResult()
        } catch (t: Throwable) {
            t.toFailedResult()
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun Response<T>.getResult(): Result<T> = withContext(callScope.coroutineContext) {
        if (isSuccessful) {
            try {
                Result.Success(body()!!)
            } catch (t: Throwable) {
                t.toFailedResult()
            }
        } else {
            val errorBody = errorBody()

            if (errorBody != null) {
                Result.Failure(parser.toError(errorBody))
            } else {
                Result.Failure(parser.toError(raw()))
            }
        }
    }
}
