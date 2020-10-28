package io.getstream.chat.android.client.call

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

internal abstract class ChatCallImpl<T : Any> : Call<T> {

    @Volatile
    protected var canceled = false
    protected var errorHandler: ((ChatError) -> Unit)? = null
    protected var nextHandler: ((T) -> Unit)? = null

    abstract override fun execute(): Result<T>

    abstract override fun enqueue(callback: (Result<T>) -> Unit)

    override fun cancel() {
        canceled = true
    }
}

internal fun <T : Any, K : Any> Call<T>.map(mapper: (T) -> K): Call<K> {
    return MapCall(this, mapper)
}

internal fun <T : Any, K : Any> Call<T>.zipWith(call: Call<K>): Call<Pair<T, K>> {
    return ZipCall.zip(this, call)
}
