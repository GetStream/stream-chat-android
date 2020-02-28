package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

interface Call<T> {
    fun execute(): Result<T>
    fun enqueue(callback: (Result<T>) -> Unit)
    fun cancel()
    fun <K> map(mapper: (T) -> K): Call<K>
    fun onError(handler: (ChatError) -> Unit): Call<T>
    fun onNext(handler: (T) -> Unit): Call<T>
    fun <K> zipWith(call: Call<K>): Call<Pair<T, K>>
}