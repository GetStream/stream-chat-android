package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLogger
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

internal class HttpLoggingInterceptor : Interceptor {

    private val logger = ChatLogger.get("Http")

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val level = logger.getLevel()

        val request = chain.request()
        if (level == ChatLogLevel.NOTHING) {
            return chain.proceed(request)
        }

        val requestBody = request.body

        val connection = chain.connection()
        var requestStartMessage =
            ("--> ${request.method} ${request.url}${if (connection != null) " " + connection.protocol() else ""}")
        if (requestBody != null) {
            requestStartMessage += " (${requestBody.contentLength()}-byte body)"
        }
        logger.logI(requestStartMessage)

        if (requestBody == null) {
            logger.logI("--> END ${request.method}")
        } else if (bodyHasUnknownEncoding(request.headers)) {
            logger.logI("--> END ${request.method} (encoded body omitted)")
        } else if (requestBody.isDuplex()) {
            logger.logI("--> END ${request.method} (duplex request body omitted)")
        } else if (requestBody.isOneShot()) {
            logger.logI("--> END ${request.method} (one-shot body omitted)")
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)

            val contentType = requestBody.contentType()
            val charset: Charset = contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8

            logger.logI("")
            if (buffer.isProbablyUtf8()) {
                logger.logI(buffer.readString(charset))
                logger.logI("--> END ${request.method} (${requestBody.contentLength()}-byte body)")
            } else {
                logger.logI(
                    "--> END ${request.method} (binary ${requestBody.contentLength()}-byte body omitted)"
                )
            }
        }

        val startNs = System.nanoTime()
        val response: Response
        try {
            response = chain.proceed(request)
        } catch (e: Exception) {
            logger.logI("<-- HTTP FAILED: $e")
            throw e
        }

        val tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)

        val responseBody = response.body!!
        val contentLength = responseBody.contentLength()
        val bodySize = if (contentLength != -1L) "$contentLength-byte" else "unknown-length"
        logger.logI(
            "<-- ${response.code}${if (response.message.isEmpty()) "" else ' ' + response.message} ${response.request.url} (${tookMs}ms${", $bodySize body"})"
        )

        if (!response.promisesBody()) {
            logger.logI("<-- END HTTP")
        } else if (bodyHasUnknownEncoding(response.headers)) {
            logger.logI("<-- END HTTP (encoded body omitted)")
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
                logger.logI("")
                logger.logI("<-- END HTTP (binary ${buffer.size}-byte body omitted)")
                return response
            }

            if (gzippedLength != null) {
                logger.logI("<-- END HTTP (${buffer.size}-byte, $gzippedLength-gzipped-byte body omitted)")
            } else {
                logger.logI("<-- END HTTP (${buffer.size}-byte body omitted)")
            }
        }

        return response
    }

    private fun bodyHasUnknownEncoding(headers: Headers): Boolean {
        val contentEncoding = headers["Content-Encoding"] ?: return false
        return !contentEncoding.equals("identity", ignoreCase = true) &&
            !contentEncoding.equals("gzip", ignoreCase = true)
    }

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
