package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

internal class ErrorCall<T : Any>(private val e: ChatError) : Call<T> {
    override fun cancel() {
        // Empty
    }

    override fun execute(): Result<T> {
        return Result(null, e)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(null, e))
    }
}
