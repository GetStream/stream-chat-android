package io.getstream.chat.android.test

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.utils.Result

public class TestCall<T : Any>(public val result: Result<T>) : Call<T> {
    public var cancelled: Boolean = false

    override fun cancel() {
        cancelled = true
    }

    override fun enqueue(callback: Call.Callback<T>) {
        callback.onResult(result)
    }

    override fun execute(): Result<T> {
        return result
    }
}

public fun <T : Any> callFrom(valueProvider: () -> T): Call<T> = TestCall(Result(valueProvider()))

public fun <T : Any> T.asCall(): Call<T> = TestCall(Result(this))
