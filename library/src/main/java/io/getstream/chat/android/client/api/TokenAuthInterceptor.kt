package io.getstream.chat.android.client.api

import android.util.Log
import io.getstream.chat.android.client.token.TokenProvider.TokenProviderListener
import io.getstream.chat.android.client.errors.ErrorCode
import io.getstream.chat.android.client.parser.JsonParser
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch


class TokenAuthInterceptor internal constructor(
    private val config: ChatConfig,
    private val parser: JsonParser
) : Interceptor {

    private val TAG = javaClass.simpleName
    private var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {

        if (config.isAnonymous) {
            return chain.proceed(chain.request())
        } else {
            val latch = CountDownLatch(if (token == null) 1 else 0)
            if (token == null) {
                config.tokenProvider.getToken(object : TokenProviderListener {
                    override fun onSuccess(token: String) {
                        this@TokenAuthInterceptor.token = token
                        latch.countDown()
                    }

                })
            }
            try {
                latch.await()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            val request: Request = addTokenHeader(chain.request())
            var response: Response = chain.proceed(request)

            if (!response.isSuccessful) {

                val err = parser.toError(response)
                if (err.streamCode == ErrorCode.TOKEN_EXPIRED.value) {
                    Log.d(TAG, "Retrying new request")
                    token = null // invalidate local cache
                    config.tokenProvider.tokenExpired()
                    response.close()
                    response = chain.proceed(request)
                } else {
                    throw IOException(err.message, err.cause)
                }
            }
            return response
        }
    }

    private fun addTokenHeader(req: Request): Request {
        return req.newBuilder().header("Authorization", token!!).build()
    }

}
