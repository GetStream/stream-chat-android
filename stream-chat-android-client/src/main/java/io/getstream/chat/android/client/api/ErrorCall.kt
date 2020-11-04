package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ErrorCall<T : Any>(private val e: ChatError) : Call<T> {
    override fun cancel() {
        // Not supported
    }

    override fun execute(): Result<T> {
        return Result(null, e)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            callback(Result(null, e))
        }
    }
}
