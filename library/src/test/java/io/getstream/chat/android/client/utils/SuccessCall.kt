package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.call.ChatCallImpl
import io.getstream.chat.android.client.errors.ChatError


class SuccessCall<T>(val result: T) : ChatCallImpl<T>() {
    override fun execute(): Result<T> {
        return Result(result, null)
    }

    override fun enqueue(callback: (Result<T>) -> Unit) {
        callback(Result(result, null))
    }

    override fun onError(errorHandler: (ChatError) -> Unit) {

    }
}