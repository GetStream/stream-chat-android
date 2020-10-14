package io.getstream.chat.android.client.call

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

public interface Call<T : Any> {

    @WorkerThread
    public fun execute(): Result<T>

    @UiThread
    public fun enqueue(callback: (Result<T>) -> Unit = {})

    public fun cancel()

    public fun <K : Any> map(mapper: (T) -> K): Call<K>
    public fun onError(handler: (ChatError) -> Unit): Call<T>
    public fun onSuccess(handler: (T) -> Unit): Call<T>
    public fun <K : Any> zipWith(call: Call<K>): Call<Pair<T, K>>
    public fun <C : Any, B : Any> zipWith(callK: Call<C>, callP: Call<B>): Call<Triple<T, C, B>>
}
