package io.getstream.chat.android.client.call

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

interface Call<T> {

    @WorkerThread
    fun execute(): Result<T>

    @UiThread
    fun enqueue(callback: (Result<T>) -> Unit = {})

    fun cancel()
    fun <K> map(mapper: (T) -> K): Call<K>
    fun onError(handler: (ChatError) -> Unit): Call<T>
    fun onSuccess(handler: (T) -> Unit): Call<T>
    fun <K> zipWith(call: Call<K>): Call<Pair<T, K>>
    fun <C, B> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>>
    fun enqueue() {

    }
}