package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.call.ChatCallImpl
import io.getstream.chat.android.client.errors.ChatError


class ErrorCall<T>(val t: ChatError) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        return Result(null, t)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(null, t))
    }

    override fun onError(errorHandler: (ChatError) -> Unit) {
        errorHandler(t)
    }
}