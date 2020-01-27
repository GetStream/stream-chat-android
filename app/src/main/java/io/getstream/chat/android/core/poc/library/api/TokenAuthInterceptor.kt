package io.getstream.chat.android.core.poc.library.api

import android.util.Log
import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.TokenProvider.TokenProviderListener
import io.getstream.chat.android.core.poc.library.socket.ErrorResponse
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.CountDownLatch


class TokenAuthInterceptor internal constructor(
    private val tokenProvider: CachedTokenProvider? = null,
    private val anonymousAuth: Boolean?
) :
    Interceptor {
    private val TAG = javaClass.simpleName
    private var token: String? = null
    override fun intercept(chain: Interceptor.Chain): Response {

        if (anonymousAuth == true) {
            return chain.proceed(chain.request())
        } else {
            val latch = CountDownLatch(if (token == null) 1 else 0)
            if (token == null) {
                tokenProvider?.getToken(object : TokenProviderListener {
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
            var request: Request = chain.request()
            request = addTokenHeader(request)
            var response: Response = chain.proceed(request)

            // check the error and only hit this path if the token was expired (error response code)
            if (!response.isSuccessful) {
                val err = ErrorResponse.parseError(response)
                if (err.streamCode == ErrorResponse.TOKEN_EXPIRED_CODE) {
                    Log.d(TAG, "Retrying new request")
                    token = null // invalidate local cache
                    tokenProvider?.tokenExpired()
                    response.close()
                    response = chain.proceed(request)
                }
            }
            return response
        }
    }

    private fun addTokenHeader(req: Request): Request {
        return req.newBuilder().header("Authorization", token!!).build()
    }

}
