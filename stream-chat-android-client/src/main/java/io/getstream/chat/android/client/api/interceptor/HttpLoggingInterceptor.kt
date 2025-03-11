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

package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.core.internal.StreamHandsOff
import io.getstream.chat.android.models.Constants
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer
import okio.GzipSource
import java.io.EOFException
import java.io.IOException
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

/**
 * Interceptor logging the requests made to the server.
 */
internal class HttpLoggingInterceptor : Interceptor {

    private val logger by taggedLogger("Chat:Http")

    @Throws(IOException::class)
    @Suppress("LongMethod", "ComplexMethod", "ReturnCount", "TooGenericExceptionCaught", "ReturnCount")
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        if (!StreamLog.isInstalled) {
            return chain.proceed(request)
        }

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        logger.i { requestStartMessage }

        if (requestBody == null) {
            logger.i { "--> END ${request.method}" }
        } else if (bodyHasUnknownEncoding(request.headers)) {
            logger.i { "--> END ${request.method} (encoded body omitted)" }
        } else if (requestBody.isDuplex()) {
            logger.i { "--> END ${request.method} (duplex request body omitted)" }
        } else if (requestBody.isOneShot()) {
            logger.i { "--> END ${request.method} (one-shot body omitted)" }
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            logger.i { "" }
            if (buffer.isProbablyUtf8()) {
                logRequestBody(buffer, charset)
                logger.i { "--> END ${request.method} (${requestBody.contentLength()}-byte body)" }
            } else {
                logger.i {
                    "--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)"
                }
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.i { "<-- HTTP FAILED: $e" }
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.i {
            "<-- ${response.code}${
                if (response.message.isEmpty()) {
                    ""
                } else {
                    ' ' +
                        response.message
                }
            } ${response.request.url} (${tookMs}ms${", $bodySize body"})"
        }

        if (!response.promisesBody()) {
            logger.i { "<-- END HTTP" }
        } else if (bodyHasUnknownEncoding(response.headers)) {
            logger.i { "<-- END HTTP (encoded body omitted)" }
        } else {
            val source = responseBody.source()
            source.request(Long.MAX_VALUE) // Buffer the entire body.
            var buffer = source.buffer

            var gzippedLength: Long? = null
            if (response.headers["Content-Encoding"].equals("gzip", ignoreCase = true)) {
                gzippedLength = buffer.size
                GzipSource(buffer.clone()).use { gzippedResponseBody ->
                    buffer = Buffer()
                    buffer.writeAll(gzippedResponseBody)
                }
            }

            if (!buffer.isProbablyUtf8()) {
                logger.i { "" }
                logger.i { "<-- END HTTP (binary ${buffer.size}-byte body omitted)" }
                return response
            }

            if (gzippedLength != null) {
                logger.i { "<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body omitted)" }
            } else {
                logger.i { "<-- END HTTP (${buffer.size}-byte body omitted)" }
            }
        }

        return response
    }

    @StreamHandsOff(
        reason = "Request body shouldn't be log entirely as it might produce OutOfMemory " +
            "exceptions when sending big files." +
            " The log will be limited to ${Constants.MAX_REQUEST_BODY_LENGTH} bytes.",
    )
    private fun logRequestBody(buffer: Buffer, charset: Charset) {
        logger.i { buffer.readString(minOf(buffer.size, Constants.MAX_REQUEST_BODY_LENGTH), charset) }
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
            !contentEncoding.equals("gzip", ignoreCase = true)
    }

    @Suppress("UnusedPrivateMember", "ReturnCount", "MagicNumber")
    private fun Buffer.isProbablyUtf8(): Boolean {
        try {
            val prefix = Buffer()
            val byteCount = size.coerceAtMost(64)
            copyTo(prefix, 0, byteCount)
            for (i in 0 until 16) {
                if (prefix.exhausted()) {
                    break
                }
                val codePoint = prefix.readUtf8CodePoint()
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false
                }
            }
            return true
        } catch (_: EOFException) {
            return false // Truncated UTF-8 sequence.
        }
    }
}
