package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import okhttp3.Interceptor
import okhttp3.Response

internal class ApiRequestAnalyserInterceptor(private val requestsAnalyser: ApiRequestsAnalyser): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        requestsAnalyser.registerRequest(request.url.toString(), mapOf("body" to request.body.toString()))

        return chain.proceed(request)
    }
}
