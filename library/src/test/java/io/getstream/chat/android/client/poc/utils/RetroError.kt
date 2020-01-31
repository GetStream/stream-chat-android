package io.getstream.chat.android.client.poc.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RetroError<T>(val statusCode: Int) : Call<T> {
    override fun enqueue(callback: Callback<T>) {
        callback.onResponse(this, execute())
    }

    override fun isExecuted(): Boolean {
        return true
    }

    override fun clone(): Call<T> {
        return this
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun cancel() {

    }

    override fun execute(): Response<T> {
        return Response.error(
            statusCode,
            "{Server error}".toResponseBody("text/plain".toMediaType())
        )
    }

    override fun request(): Request {
        return null!!
    }

}