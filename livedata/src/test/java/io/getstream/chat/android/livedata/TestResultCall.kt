package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.errors.ChatError

class TestResultCall<T>(val result: Result<T>) : Call<T> {
    override fun cancel() { }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(result)
    }

    override fun execute(): Result<T> = result

    override fun <K> map(mapper: (T) -> K): Call<K> {
        TODO("Not yet implemented")
    }

    override fun onError(handler: (ChatError) -> Unit): Call<T> {
        TODO("Not yet implemented")
    }

    override fun onSuccess(handler: (T) -> Unit): Call<T> {
        TODO("Not yet implemented")
    }

    override fun <C, B> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>> {
        TODO("Not yet implemented")
    }

    override fun <K> zipWith(call: Call<K>): Call<Pair<T, K>> {
        TODO("Not yet implemented")
    }

}