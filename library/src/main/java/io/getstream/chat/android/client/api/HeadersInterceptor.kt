package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.ChatClientBuilder
import okhttp3.Interceptor
import okhttp3.Response

class HeadersInterceptor(val config: ChatClientBuilder.ChatConfig) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val authType = if (config.isAnonimous) "anonymous" else "jwt"
        val request = chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("Accept-Encoding", "application/gzip")
            .build()
        return chain.proceed(request)
    }
}