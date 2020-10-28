package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.utils.Result

internal class TestCall<T : Any>(val result: Result<T>) : Call<T> {
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
}
