package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.call.RetrofitCallBlah
import io.getstream.chat.android.client.parser2.MoshiChatParser
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class RetroError<T : Any>(val statusCode: Int) : Call<T> {

    fun toRetrofitCall(): RetrofitCallBlah<T> {
        return RetrofitCallBlah(
            call = this,
            parser = MoshiChatParser(),
            callbackExecutor = { runnable -> runnable.run() },
        )
    }

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

    override fun timeout(): Timeout {
        return Timeout()
    }
}
