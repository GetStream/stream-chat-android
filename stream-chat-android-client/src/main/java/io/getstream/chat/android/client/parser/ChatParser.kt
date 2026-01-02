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

package io.getstream.chat.android.client.parser

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.fromChatErrorCode
import io.getstream.chat.android.client.socket.ErrorDetail
import io.getstream.chat.android.client.socket.ErrorResponse
import io.getstream.log.StreamLog
import io.getstream.result.Error
import io.getstream.result.Result
import okhttp3.Response
import okhttp3.ResponseBody
import retrofit2.Retrofit

internal interface ChatParser {

    private val tag: String get() = "Chat:ChatParser"

    fun toJson(any: Any): String
    fun <T : Any> fromJson(raw: String, clazz: Class<T>): T
    fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder

    @Suppress("TooGenericExceptionCaught")
    fun <T : Any> fromJsonOrError(raw: String, clazz: Class<T>): Result<T> {
        return try {
            Result.Success(fromJson(raw, clazz))
        } catch (expected: Throwable) {
            Result.Failure(
                Error.ThrowableError("fromJsonOrError error parsing of $clazz into $raw", expected),
            )
        }
    }

    @Suppress("TooGenericExceptionCaught", "NestedBlockDepth")
    fun toError(okHttpResponse: Response): Error.NetworkError {
        val statusCode: Int = okHttpResponse.code

        return try {
            // Try to parse default Stream error body
            val body = okHttpResponse.peekBody(Long.MAX_VALUE).string()

            if (body.isEmpty()) {
                Error.NetworkError.fromChatErrorCode(
                    chatErrorCode = ChatErrorCode.NO_ERROR_BODY,
                    statusCode = statusCode,
                )
            } else {
                val error = try {
                    fromJson(body, ErrorResponse::class.java)
                } catch (_: Throwable) {
                    ErrorResponse().apply { message = body }
                }
                Error.NetworkError(
                    serverErrorCode = error.code,
                    message = error.message +
                        moreInfoTemplate(error.moreInfo) +
                        buildDetailsTemplate(error.details),
                    statusCode = statusCode,
                )
            }
        } catch (expected: Throwable) {
            StreamLog.e(tag, expected) { "[toError] failed" }
            Error.NetworkError.fromChatErrorCode(
                chatErrorCode = ChatErrorCode.NETWORK_FAILED,
                cause = expected,
                statusCode = statusCode,
            )
        }
    }

    fun toError(errorResponseBody: ResponseBody): Error.NetworkError {
        return try {
            val errorResponse: ErrorResponse = fromJson(errorResponseBody.string(), ErrorResponse::class.java)
            val (code, message, statusCode, _, moreInfo) = errorResponse

            Error.NetworkError(
                serverErrorCode = code,
                message = message + moreInfoTemplate(moreInfo),
                statusCode = statusCode,
            )
        } catch (expected: Throwable) {
            StreamLog.e(tag, expected) { "[toError] failed" }
            Error.NetworkError.fromChatErrorCode(
                chatErrorCode = ChatErrorCode.NETWORK_FAILED,
                cause = expected,
            )
        }
    }

    private fun moreInfoTemplate(moreInfo: String): String {
        return if (moreInfo.isNotBlank()) {
            "\nMore information available at $moreInfo"
        } else {
            ""
        }
    }

    private fun buildDetailsTemplate(details: List<ErrorDetail>): String {
        return if (details.isNotEmpty()) {
            "\nError details: $details"
        } else {
            ""
        }
    }
}
