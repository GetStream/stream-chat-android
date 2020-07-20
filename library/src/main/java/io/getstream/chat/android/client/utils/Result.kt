package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

data class Result<T>(
    private val data: T?,
    private val error: ChatError?
) {

    constructor(data: T) : this(data, null)
    constructor(error: ChatError) : this(null, error)

    val isSuccess: Boolean
        get() = data != null

    val isError: Boolean
        get() = error != null

    fun data(): T {
        if (data == null) throw IllegalStateException("Result is not successful. Check result.isSuccess before getting data: result.data()")
        return data
    }

    fun error(): ChatError {
        if (error == null) throw IllegalStateException("Result is successful, no error. Check result.isSuccess before getting error: result.error()")
        return error
    }
}
