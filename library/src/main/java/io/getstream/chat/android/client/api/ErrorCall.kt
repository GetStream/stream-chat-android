package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.call.ChatCallImpl
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.utils.Result

class ErrorCall<T>(val e: ChatError) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        return Result(null, e)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(null, e))
    }

    override fun enqueue() {
        enqueue {
            // ignore result
        }
    }

}