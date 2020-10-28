package io.getstream.chat.android.client.call

import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import io.getstream.chat.android.client.utils.Result

public interface Call<T : Any> {

    @WorkerThread
    public fun execute(): Result<T>

    @UiThread
    public fun enqueue(callback: (Result<T>) -> Unit = {})

    public fun cancel()

}

internal fun <T : Any, K : Any> Call<T>.map(mapper: (T) -> K): Call<K> {
    return MapCall(this, mapper)
}

internal fun <T : Any, K : Any> Call<T>.zipWith(call: Call<K>): Call<Pair<T, K>> {
    return ZipCall.zip(this, call)
}
