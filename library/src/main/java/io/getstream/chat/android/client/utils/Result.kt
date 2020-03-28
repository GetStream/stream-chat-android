package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError
import java.lang.IllegalStateException

data class Result<T>(
    private val data: T?,
    private val error: ChatError?
) {

    val isSuccess: Boolean
        get() = data != null

    val isError: Boolean
        get() = error != null

    fun data(): T {
        if (data == null) throw IllegalStateException("Result is not successful. Check result.isSuccess before getting data: result.data()")
        return data
    }

    fun error(): ChatError {
        return error!!
    }
}