package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

public data class Result<T : Any>(
    private val data: T?,
    private val error: ChatError?
) {

    internal constructor(data: T) : this(data, null)
    internal constructor(error: ChatError) : this(null, error)

    val isSuccess: Boolean
        get() = data != null

    val isError: Boolean
        get() = error != null

    public fun data(): T {
        return checkNotNull(data) { "Result is not successful. Check result.isSuccess before reading the data." }
    }

    public fun error(): ChatError {
        return checkNotNull(error) { "Result is successful, not an error. Check result.isSuccess before reading the error." }
    }
}
