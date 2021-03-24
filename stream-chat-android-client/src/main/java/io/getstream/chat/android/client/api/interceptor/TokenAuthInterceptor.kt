package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal class TokenAuthInterceptor internal constructor(
    private val tokenManager: TokenManager,
    private val parser: ChatParser,
    private val isAnonymous: () -> Boolean,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isAnonymous()) {
            return chain.proceed(chain.request())
        } else {
            if (!tokenManager.hasTokenProvider()) {
                val description = ChatErrorCode.UNDEFINED_TOKEN.description
                val code = ChatErrorCode.UNDEFINED_TOKEN.code
                throw ChatRequestError(description, code, -1)
            }

            tokenManager.ensureTokenLoaded()

            val request: Request = addTokenHeader(chain.request())
            var response: Response = chain.proceed(request)

            if (!response.isSuccessful) {
                val err = parser.toError(response)
                if (err.streamCode == ChatErrorCode.TOKEN_EXPIRED.code) {
                    tokenManager.expireToken()
                    tokenManager.loadSync()
                    response.close()
                    response = chain.proceed(request)
                } else {
                    throw ChatRequestError(err.description, err.streamCode, err.statusCode, err.cause)
                }
            }
            return response
        }
    }

    private fun addTokenHeader(request: Request): Request {
        val token = tokenManager.getToken()
        try {
            return request.newBuilder().header(AUTH_HEADER, token).build()
        } catch (e: Throwable) {
            val description = ChatErrorCode.INVALID_TOKEN.description
            val code = ChatErrorCode.INVALID_TOKEN.code
            throw ChatRequestError("$description: $token", code, -1, e)
        }
    }

    companion object {
        const val AUTH_HEADER = "Authorization"
    }
}
