package io.getstream.chat.android.core.poc.library.api

import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(val isAnonymous: () -> Boolean) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (isAnonymous()) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("Accept-Encoding", "application/gzip")
            .build()
        return chain.proceed(request)
    }
}