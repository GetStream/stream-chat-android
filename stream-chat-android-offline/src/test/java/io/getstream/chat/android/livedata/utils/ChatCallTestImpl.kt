package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

internal class ChatCallTestImpl<T : Any>(val result: Result<T>) : Call<T> {
    var cancelled = false

    override fun cancel() {
        cancelled = true
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(result)
    }

    override fun execute(): Result<T> {
        return result
    }

    override fun <K : Any> map(mapper: (T) -> K): Call<K> {
        val mapped: K = mapper(result.data())
        val newResult = Result(mapped, null)
        return ChatCallTestImpl(newResult)
    }

    override fun onError(handler: (ChatError) -> Unit): Call<T> {
        return this
    }

    override fun onSuccess(handler: (T) -> Unit): Call<T> {
        return this
    }

    override fun <C : Any, B : Any> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun <K : Any> zipWith(call: Call<K>): Call<Pair<T, K>> {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }
}
