package io.getstream.chat.android.client.api.interceptor

import io.getstream.chat.android.client.plugins.requests.ApiRequestsAnalyser
import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import java.io.OutputStream

internal class ApiRequestAnalyserInterceptor(private val requestsAnalyser: ApiRequestsAnalyser): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val buffer = Buffer()
        val stringOutputStream = StringOutputStream()

        chain.request().body?.writeTo(buffer)
        buffer.writeTo(stringOutputStream)

        requestsAnalyser.registerRequest(request.url.toString(), mapOf("body" to stringOutputStream.toString()))

        return chain.proceed(request)
    }
}

private class StringOutputStream(): OutputStream() {

    private val stringBuilder = StringBuilder()

    override fun write(b: Int) {
        stringBuilder.append(b.toChar())
    }

    override fun toString(): String = stringBuilder.toString().ifEmpty { "no_body" }
}
