package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.errors.ChatError

data class Result<T>(
    private val data: T?,
    private val error: ChatError?
) {
    fun isSuccess(): Boolean {
        return data != null
    }

    fun data(): T {
        return data!!
    }

    fun error(): ChatError {
        return error!!
    }
}