package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 *  A class which encapsulates a successful outcome with a value of type [T] or a failure with [ChatError].
 */
public class Result<T : Any> private constructor(
    private val data: T?,
    private val error: ChatError?,
) {

    @Suppress("DEPRECATION")
    public constructor(data: T) : this(data, null)

    @Suppress("DEPRECATION")
    public constructor(error: ChatError) : this(null, error)

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

    public companion object {

        /**
         * Creates a [Result] object with [data] payload
         */
        @JvmStatic
        public fun <T : Any> success(data: T): Result<T> {
            return Result(data)
        }

        /**
         * Creates a [Result] object with error payload
         */
        @JvmStatic
        public fun <T : Any> error(t: Throwable): Result<T> {
            return Result(null, ChatError(t.message, t))
        }

        /**
         * Creates a [Result] object with error payload
         */
        @JvmStatic
        public fun <T : Any> error(error: ChatError): Result<T> {
            return Result(null, error)
        }
    }
}

public fun <T : Any, K : Any> Result<T>.map(mapper: (T) -> K): Result<K> {
    return if (isSuccess) {
        Result(mapper(data()))
    } else {
        Result(error())
    }
}

@InternalStreamChatApi
public fun Result<*>.toUnitResult(): Result<Unit> = map {}

public fun <T: Any> Result<T>.onSuccess(successSideEffect: (T) -> Unit): Result<T> {
    if (isSuccess) {
        successSideEffect(data())
    }
    return this
}

public suspend fun <T : Any, K : Any> Result<T>.mapSuspend(mapper: suspend (T) -> K): Result<K> {
    return if (isSuccess) {
        Result(mapper(data()))
    } else {
        Result(error())
    }
}

public fun <T: Any> Result<T>.mapError(errorMapper: (ChatError) -> T): Result<T> {
    return if (isSuccess) {
        this
    } else {
        Result(errorMapper(error()))
    }
}

public suspend fun <T: Any> Result<T>.mapErrorSuspend(errorMapper: suspend (ChatError) -> T): Result<T> {
    return if (isSuccess) {
        this
    } else {
        Result(errorMapper(error()))
    }
}

public fun <T: Any> Result<T>.onError(errorSideEffect: (ChatError) -> Unit): Result<T> {
    if (isError) {
        errorSideEffect(error())
    }
    return this
}
