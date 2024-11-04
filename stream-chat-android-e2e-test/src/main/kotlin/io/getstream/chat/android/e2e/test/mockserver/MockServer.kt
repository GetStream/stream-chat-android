package io.getstream.chat.android.e2e.test.mockserver

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody

public var mockServerUrl: String? = null
private const val driverUrl: String = "http://10.0.2.2:4567"
private val okHttp: OkHttpClient = OkHttpClient()

public class MockServer {

    public fun start() {
        val request = Request.Builder().url("$driverUrl/start").build()
        val response = okHttp.newCall(request).execute()
        val mockServerPort = response.body?.string().toString()
        val driverPort = driverUrl.split(":").last()
        mockServerUrl = driverUrl.replace(driverPort, mockServerPort)
    }

    public fun stop() {
        getRequest("stop")
    }

    public fun postRequest(
        endpoint: String,
        body: RequestBody = "".toRequestBody("text".toMediaTypeOrNull())
    ): ResponseBody? {
        val request = Request.Builder()
            .url("$mockServerUrl/$endpoint")
            .post(body)
            .build()
        val response = okHttp.newCall(request).execute()
        return response.body
    }

    public fun getRequest(endpoint: String): ResponseBody? {
        val request = Request.Builder()
            .url("$mockServerUrl/$endpoint")
            .build()
        val response = okHttp.newCall(request).execute()
        return response.body
    }
}
