package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.errors.ChatErrorCode
import io.getstream.chat.android.client.errors.ChatRequestError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.token.TokenProvider.TokenProviderListener
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response


class TokenAuthInterceptor internal constructor(
    private val config: ChatClientConfig,
    private val parser: ChatParser
) : Interceptor {

    private var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {

        if (config.isAnonymous) {
            return chain.proceed(chain.request())
        } else {
            if (token == null) {
                config.tokenProvider.getToken(object : TokenProviderListener {
                    override fun onSuccess(token: String) {
                        this@TokenAuthInterceptor.token = token
                    }

                })
            }

            if (token == null) {
                val description = ChatErrorCode.UNDEFINED_TOKEN.description
                val code = ChatErrorCode.UNDEFINED_TOKEN.code
                throw ChatRequestError(description, code, -1)
            }

            val request: Request = addTokenHeader(chain.request())
            var response: Response = chain.proceed(request)

            if (!response.isSuccessful) {

                val err = parser.toError(response)
                if (err.streamCode == ChatErrorCode.TOKEN_EXPIRED.code) {
                    token = null
                    config.tokenProvider.tokenExpired()
                    response.close()
                    response = chain.proceed(request)
                } else {
                    throw ChatRequestError(err.description, err.streamCode, err.statusCode, err)
                }
            }
            return response
        }
    }

    private fun addTokenHeader(req: Request): Request {
        try {
            return req.newBuilder().header("Authorization", token!!).build()
        } catch (e: Throwable) {
            val description = ChatErrorCode.INVALID_TOKEN.description
            val code = ChatErrorCode.INVALID_TOKEN.code
            throw ChatRequestError("$description: $token", code, -1, e)
        }
    }

}
