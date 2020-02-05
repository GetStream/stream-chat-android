package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

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