package io.getstream.chat.android.client.api

import okhttp3.*
import java.util.concurrent.TimeUnit

class FakeChain(vararg val response: FakeResponse) : Interceptor.Chain {

    var chainIndex = 0

    fun processChain() {
        chainIndex++
    }

    override fun call(): Call {
        return null!!
    }

    override fun connectTimeoutMillis(): Int {
        return 0
    }

    override fun connection(): Connection? {
        return null
    }

    override fun proceed(request: Request): Response {

        val response = response[chainIndex]

        return Response.Builder()
            .code(response.statusCode)
            .request(request)
            .protocol(Protocol.HTTP_2)
            .body(response.body)
            .message("ok")
            .build()
    }

    override fun readTimeoutMillis(): Int {
        return 0
    }

    override fun request(): Request {
        return Request.Builder()
            .url("https://hello.url")
            .build()
    }

    override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
        return this
    }

    override fun writeTimeoutMillis(): Int {
        return 0
    }

}