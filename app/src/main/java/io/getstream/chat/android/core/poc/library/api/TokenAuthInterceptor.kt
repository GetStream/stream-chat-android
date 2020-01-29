package io.getstream.chat.android.core.poc.library.api

import android.util.Log
import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider.TokenProviderListener
import io.getstream.chat.android.core.poc.library.gson.JsonParserImpl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.CountDownLatch


class TokenAuthInterceptor internal constructor(
    private val tokenProvider: () -> CachedTokenProvider?,
    private val anonymousAuth: () -> Boolean,
    private val jsonParser: JsonParserImpl
) : Interceptor {

    private val TOKEN_EXPIRED_CODE = 40
    private val TAG = javaClass.simpleName
    private var token: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {

        if (anonymousAuth()) {
            return chain.proceed(chain.request())
        } else {
            val latch = CountDownLatch(if (token == null) 1 else 0)
            if (token == null) {
                tokenProvider()?.getToken(object : TokenProviderListener {
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

                val err = jsonParser.toError(response)
                if (err.streamCode == TOKEN_EXPIRED_CODE) {
                    Log.d(TAG, "Retrying new request")
                    token = null // invalidate local cache
                    tokenProvider()?.tokenExpired()
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
