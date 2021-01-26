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
            val error = toError(body)

            if (error == null) {
                ChatNetworkError.create(ChatErrorCode.NO_ERROR_BODY, statusCode = statusCode)
            } else {
                ChatNetworkError.create(error.code, error.message, statusCode)
            }
        } catch (t: Throwable) {
            ChatLogger.instance.logE(TAG, t)
            ChatNetworkError.create(ChatErrorCode.NETWORK_FAILED, t, statusCode)
        }
    }

    private fun toError(body: String?): ErrorResponse? {
        if (body.isNullOrEmpty()) return ErrorResponse(message = "Body is null or empty")

        return try {
            fromJson(body, ErrorResponse::class.java)
        } catch (t: Throwable) {
            ErrorResponse().apply { message = body }
        }
    }
}
