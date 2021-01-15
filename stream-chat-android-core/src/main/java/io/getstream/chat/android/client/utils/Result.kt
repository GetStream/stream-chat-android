package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

public class Result<T : Any> private constructor(
    private val data: T?,
    private val error: ChatError?
) {

    @Suppress("DEPRECATION")
    public constructor(data: T) : this(data, null)

    @Suppress("DEPRECATION")
    public constructor(error: ChatError)  : this(null, error)

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Result<*>

        if (data != other.data) return false
        if (error != other.error) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data?.hashCode() ?: 0
        result = 31 * result + (error?.hashCode() ?: 0)
        return result
    }
}
