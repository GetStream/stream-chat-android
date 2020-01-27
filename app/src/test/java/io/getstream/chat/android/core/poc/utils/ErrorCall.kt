package io.getstream.chat.android.core.poc.utils

import io.getstream.chat.android.core.poc.library.Result
import io.getstream.chat.android.core.poc.library.call.ChatCallImpl
import io.getstream.chat.android.core.poc.library.errors.ChatError


class ErrorCall<T>(val t: ChatError) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        return Result(null, t)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(null, t))
    }
}