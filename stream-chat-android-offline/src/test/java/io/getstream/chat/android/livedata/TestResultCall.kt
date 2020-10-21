package io.getstream.chat.android.livedata

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

internal class TestResultCall<T : Any>(val result: Result<T>) : Call<T> {
    override fun cancel() {}

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(result)
    }

    override fun execute(): Result<T> = result

    override fun <K : Any> map(mapper: (T) -> K): Call<K> {
        TODO("Not yet implemented")
    }

    override fun onError(handler: (ChatError) -> Unit): Call<T> {
        TODO("Not yet implemented")
    }

    override fun onSuccess(handler: (T) -> Unit): Call<T> {
        TODO("Not yet implemented")
    }

    override fun <C : Any, B : Any> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>> {
        TODO("Not yet implemented")
    }

    override fun <K : Any> zipWith(call: Call<K>): Call<Pair<T, K>> {
        TODO("Not yet implemented")
    }
}
