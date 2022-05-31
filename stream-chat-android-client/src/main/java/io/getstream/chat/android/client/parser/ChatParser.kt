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
import io.getstream.chat.android.client.errors.cause.MessageModerationFailedException
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.socket.ErrorDetail
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.chat.android.client.utils.Result
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit

internal interface ChatParser {

    private val tag: String
        get() = ChatParser::class.java.simpleName

    fun toJson(any: Any): String
    fun <T : Any> fromJson(raw: String, clazz: Class<T>): T
    fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder

    @Suppress("TooGenericExceptionCaught")
    fun <T : Any> fromJsonOrError(raw: String, clazz: Class<T>): Result<T> {
        return try {
            Result(fromJson(raw, clazz))
        } catch (expected: Throwable) {
            Result(ChatError("fromJsonOrError error parsing of $clazz into $raw", expected))
        }
    }

    @Suppress("TooGenericExceptionCaught", "NestedBlockDepth")
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
                } catch (_: Throwable) {
                    ErrorResponse().apply { message = body }
                }
                val cause = error.extractCause()
                ChatNetworkError.create(
                    streamCode = error.code,
                    description = error.message + moreInfoTemplate(error.moreInfo) + detailsTemplate(error.details),
                    statusCode = statusCode,
                    cause = cause
                )
            }
        } catch (expected: Throwable) {
            ChatLogger.instance.logE(tag, expected)
            ChatNetworkError.create(
                code = ChatErrorCode.NETWORK_FAILED,
                cause = expected,
                statusCode = statusCode
            )
        }
    }

    fun toError(errorResponseBody: ResponseBody): ChatNetworkError {
        return try {
            val errorResponse: ErrorResponse = fromJson(errorResponseBody.string(), ErrorResponse::class.java)
            val (code, message, statusCode, _, moreInfo) = errorResponse

            ChatNetworkError.create(
                streamCode = code,
                description = message + moreInfoTemplate(moreInfo),
                statusCode = statusCode
            )
        } catch (expected: Throwable) {
            ChatLogger.instance.logE(tag, expected)
            ChatNetworkError.create(
                code = ChatErrorCode.NETWORK_FAILED,
                cause = expected,
                statusCode = -1
            )
        }
    }

    private fun moreInfoTemplate(moreInfo: String): String {
        return if (moreInfo.isNotBlank()) {
            "\nMore information available at $moreInfo"
        } else ""
    }

    private fun detailsTemplate(details: List<ErrorDetail>): String {
        return if (details.isNotEmpty()) {
            "\nError details: $details"
        } else ""
    }

    private fun ErrorResponse.extractCause(): Throwable? {
        if (code == ChatErrorCode.MESSAGE_MODERATION_FAILED.code) {
            return MessageModerationFailedException(
                details = details.map { detail ->
                    MessageModerationFailedException.Detail(
                        code = detail.code,
                        messages = detail.messages
                    )
                },
                message = message
            )
        }
        return null
    }
}


