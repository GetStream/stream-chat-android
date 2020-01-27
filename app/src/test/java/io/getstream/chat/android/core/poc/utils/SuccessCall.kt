package io.getstream.chat.android.core.poc.utils

import io.getstream.chat.android.core.poc.library.Result
import io.getstream.chat.android.core.poc.library.call.ChatCallImpl


class SuccessCall<T>(val result: T) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        return Result(result, null)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(result, null))
    }
}