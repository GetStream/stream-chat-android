package io.getstream.chat.android.client.api.interceptor

import okhttp3.Interceptor
import okhttp3.Response

internal class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val url = original.url.newBuilder()
            .addQueryParameter(PARAM_API_KEY, apiKey)
            .build()
        val request = original.newBuilder()
            .url(url)
            .build()
        return chain.proceed(request)
    }

    companion object {
        private const val PARAM_API_KEY = "api_key"
    }
}
