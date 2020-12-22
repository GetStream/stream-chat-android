package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ErrorCall<T : Any>(private val e: ChatError) : Call<T> {
    override fun cancel() {
        // Not supported
    }

    override fun execute(): Result<T> {
        return Result(e)
    }

    override fun enqueue(callback: Call.Callback<T>) {
        GlobalScope.launch(DispatcherProvider.Main) {
            callback.onResult(Result(e))
        }
    }
}
