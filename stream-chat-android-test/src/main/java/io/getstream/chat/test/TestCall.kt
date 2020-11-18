package io.getstream.chat.test

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.utils.Result

public class TestCall<T : Any>(public val result: Result<T>) : Call<T> {
    public var cancelled: Boolean = false

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
