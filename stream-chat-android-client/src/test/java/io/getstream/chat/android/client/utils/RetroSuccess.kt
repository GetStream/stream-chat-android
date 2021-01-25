package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.api.RetrofitCallAdapterFactory
import io.getstream.chat.android.client.call.RetrofitCall
import io.getstream.chat.android.client.parser.GsonChatParser
import okhttp3.Request
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class RetroSuccess<T : Any>(val result: T) : Call<T> {

    fun toRetrofitCall(): RetrofitCall<T> {
        return RetrofitCall(this, GsonChatParser(), RetrofitCallAdapterFactory.mainThreadExecutor)
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
        return Response.success(result)
    }

    override fun request(): Request {
        return null!!
    }

    override fun timeout(): Timeout {
        return Timeout()
    }
}
