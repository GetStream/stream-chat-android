package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

public class Result<T : Any> {
    private val data: T?
    private val error: ChatError?

    @Suppress("DEPRECATION")
    public constructor(data: T) {
        this.data = data
        this.error = null
    }

    @Suppress("DEPRECATION")
    public constructor(error: ChatError) {
        this.data = null
        this.error = error
    }

    public val isSuccess: Boolean
        get() = data != null

    public val isError: Boolean
        get() = error != null

    public fun data(): T {
        return checkNotNull(data) { "Result is not successful. Check result.isSuccess before reading the data." }
    }

    public fun error(): ChatError {
        return checkNotNull(error) { "Result is successful, not an error. Check result.isSuccess before reading the error." }
    }
}
