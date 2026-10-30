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
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CoroutineScope
import retrofit2.HttpException
import retrofit2.Response
import kotlin.coroutines.cancellation.CancellationException

/**
 * Runs a request of the generated [io.getstream.chat.android.network.apis.ChatEndpoints] as a [Call] that fails the
 * same way a [RetrofitCall] does.
 */
internal fun <T : Any> endpointCall(
    parser: ChatParser,
    scope: CoroutineScope,
    request: suspend () -> T,
): Call<T> = CoroutineCall(scope) {
    @Suppress("TooGenericExceptionCaught")
    try {
        Result.Success(request())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Result.Failure(e.response()?.let(parser::toError) ?: e.toNetworkError())
    } catch (t: Throwable) {
        Result.Failure(t.toNetworkError())
    }
}

internal fun ChatParser.toError(response: Response<*>): Error =
    response.errorBody()?.let(::toError) ?: toError(response.raw())

internal fun Throwable.toNetworkError(): Error = when (this) {
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
