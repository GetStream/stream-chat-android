package io.getstream.chat.android.core.poc.library

import io.getstream.chat.android.core.poc.library.errors.ChatError

data class Result<T>(
    private val data: T?,
    private val error: ChatError?
) {

    val isSuccess: Boolean
        get() = data != null

    fun data(): T {
        return data!!
    }

    fun error(): ChatError {
        return error!!
    }
}