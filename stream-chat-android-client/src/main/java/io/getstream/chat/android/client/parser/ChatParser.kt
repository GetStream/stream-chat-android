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

package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.utils.Result
import okhttp3.Response
import retrofit2.Retrofit

internal interface ChatParser {

    private val TAG: String
        get() = ChatParser::class.java.simpleName

    fun toJson(any: Any): String
    fun <T : Any> fromJson(raw: String, clazz: Class<T>): T
    fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder

    fun <T : Any> fromJsonOrError(raw: String, clazz: Class<T>): Result<T> {
        return try {
            Result(fromJson(raw, clazz))
        } catch (t: Throwable) {
            Result(ChatError("fromJsonOrError error parsing of $clazz into $raw", t))
        }
    }

    fun toError(okHttpResponse: Response): ChatNetworkError {
        val statusCode: Int = okHttpResponse.code

        return try {
            // Try to parse default Stream error body
            val body = okHttpResponse.peekBody(Long.MAX_VALUE).string()

            if (body.isEmpty()) {
                ChatNetworkError.create(ChatErrorCode.NO_ERROR_BODY, statusCode = statusCode)
            } else {
                val error = try {
                    fromJson(body, ErrorResponse::class.java)
                } catch (t: Throwable) {
                    ErrorResponse().apply { message = body }
                }
                ChatNetworkError.create(error.code, error.message, statusCode)
            }
        } catch (t: Throwable) {
            ChatLogger.instance.logE(TAG, t)
            ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED, t, statusCode)
        }
    }
}
