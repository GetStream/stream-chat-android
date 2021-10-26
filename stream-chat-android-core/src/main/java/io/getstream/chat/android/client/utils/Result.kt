package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlin.jvm.Throws

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

    /**
     * Returns true if a request of payload response has been successful.
     */
    public val isSuccess: Boolean
        get() = data != null

    /**
     * Returns true if a request of payload response has been failed.
     */
    public val isError: Boolean
        get() = error != null

    /**
     * Returns the successful data payload.
     */
    public fun data(): T {
        return checkNotNull(data) { "Result is not successful. Check result.isSuccess before reading the data." }
    }

    /**
     * Returns the [ChatError] error payload.
     */
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
         * Creates a [Result] object with [data] payload.
         *
         * @param data successful data payload.
         *
         * @return [Result] of [T] that contains successful data payload.
         */
        @JvmStatic
        public fun <T : Any> success(data: T): Result<T> {
            return Result(data)
        }

        /**
         * Creates a [Result] object with error payload.
         *
         * @param t Unexpected [Exception] or [Throwable].
         *
         * @return [Result] of [T] that contains [ChatError] error payload.
         */
        @JvmStatic
        public fun <T : Any> error(t: Throwable): Result<T> {
            return Result(null, ChatError(t.message, t))
        }

        /**
         * Creates a [Result] object with error payload.
         *
         * @param error [ChatError] error payload.
         *
         * @return [Result] of [T] that contains [ChatError] error payload.
         */
        @JvmStatic
        public fun <T : Any> error(error: ChatError): Result<T> {
            return Result(null, error)
        }

        /**
         * Creates a [Result] object with nullable [Any] type of data.
         *
         * @param data Any type of data for creating an instance of [Result].
         *
         * @return [Result] of [T] that contains successful data or [ChatError] error payload.
         */
        @JvmStatic
        @Throws(IllegalArgumentException::class)
        public inline fun <reified T : Any> of(data: Any): Result<T> {
            return when (data) {
                is T -> success(data)
                is Throwable -> error(data)
                is ChatError -> error(data)
                else -> throw IllegalArgumentException("Unexpected type of the data payload: $data")
            }
        }

        /**
         * Creates a [Result] object with nullable [Any] type of data.
         *
         * @param block A lambda function that returns a data for creating an instance of [Result].
         *
         * @return [Result] of [T] that contains successful data or [ChatError] error payload.
         */
        @JvmSynthetic
        public inline fun <reified T : Any> of(crossinline block: () -> Any): Result<T> {
            return of(block.invoke())
        }

        /**
         * Creates a [Result] object with nullable [Any] type of data.
         *
         * @param block A suspending lambda function that returns a data for creating an instance of [Result].
         *
         * @return [Result] of [T] that contains successful data or [ChatError] error payload.
         */
        @JvmSynthetic
        public suspend inline fun <reified T : Any> ofSuspend(crossinline block: suspend () -> Any): Result<T> {
            return of(block.invoke())
        }
    }
}

/**
 * Returns a [Result] of [Unit] from any type of a [Result].
 *
 * @return [Result] of [Unit].
 */
@InternalStreamChatApi
public fun Result<*>.toUnitResult(): Result<Unit> = map {}

/**
 * Runs the [successSideEffect] lambda function if the [Result] contains a successful data payload.
 *
 * @param successSideEffect A lambda that receives the successful data payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public fun <T : Any> Result<T>.onSuccess(successSideEffect: (T) -> Unit): Result<T> {
    if (isSuccess) {
        successSideEffect(data())
    }
    return this
}

/**
 * Runs the suspending [successSideEffect] lambda function if the [Result] contains a successful data payload.
 *
 * @param successSideEffect A suspending lambda that receives the successful data payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public suspend fun <T : Any> Result<T>.onSuccessSuspend(successSideEffect: suspend (T) -> Unit): Result<T> {
    if (isSuccess) {
        successSideEffect(data())
    }
    return this
}

/**
 * Returns a transformed [Result] of applying the given [mapper] function if the [Result]
 * contains a successful data payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param mapper A lambda for mapping [Result] of [T] to [Result] of [K].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public fun <T : Any, K : Any> Result<T>.map(mapper: (T) -> K): Result<K> {
    return if (isSuccess) {
        Result(mapper(data()))
    } else {
        Result(error())
    }
}

/**
 * Returns a transformed [Result] of applying the given suspending [mapper] function if the [Result]
 * contains a successful data payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param mapper A suspending lambda for mapping [Result] of [T] to [Result] of [K].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public suspend fun <T : Any, K : Any> Result<T>.mapSuspend(mapper: suspend (T) -> K): Result<K> {
    return if (isSuccess) {
        Result(mapper(data()))
    } else {
        Result(error())
    }
}

/**
 * Recovers the error payload by applying the given [errorMapper] function if the [Result]
 * contains an error payload.
 *
 * @param errorMapper A lambda that receives [ChatError] and transforms it as a payload [T].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public fun <T : Any> Result<T>.recover(errorMapper: (ChatError) -> T): Result<T> {
    return if (isSuccess) {
        this
    } else {
        Result(errorMapper(error()))
    }
}

/**
 * Recovers the error payload by applying the given suspending [errorMapper] function if the [Result]
 * contains an error payload.
 *
 * @param errorMapper A suspending lambda that receives [ChatError] and transforms it as a payload [T].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public suspend fun <T : Any> Result<T>.recoverSuspend(errorMapper: suspend (ChatError) -> T): Result<T> {
    return if (isSuccess) {
        this
    } else {
        Result(errorMapper(error()))
    }
}

/**
 * Runs the [errorSideEffect] lambda function if the [Result] contains an error payload.
 *
 * @param errorSideEffect A lambda that receives the [ChatError] payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public fun <T : Any> Result<T>.onError(errorSideEffect: (ChatError) -> Unit): Result<T> {
    if (isError) {
        errorSideEffect(error())
    }
    return this
}

/**
 * Runs the suspending [errorSideEffect] lambda function if the [Result] contains an error payload.
 *
 * @param errorSideEffect A suspending lambda that receives the [ChatError] payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public suspend fun <T : Any> Result<T>.onErrorSuspend(errorSideEffect: suspend (ChatError) -> Unit): Result<T> {
    if (isError) {
        errorSideEffect(error())
    }
    return this
}

/**
 * Returns a transformed [Result] from results of the [func] if the [Result] contains a successful data payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param func A lambda that returns [Result] of [R].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public fun <T : Any, R : Any> Result<T>.flatMap(func: (T) -> Result<R>): Result<R> {
    return if (isSuccess) {
        func(data())
    } else {
        Result.error(error())
    }
}

/**
 * Returns a transformed [Result] from results of the suspending [func] if the [Result] contains a successful data payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param func A suspending lambda that returns [Result] of [R].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public suspend fun <T : Any, R : Any> Result<T>.flatMapSuspend(func: suspend (T) -> Result<R>): Result<R> {
    return if (isSuccess) {
        func(data())
    } else {
        Result.error(error())
    }
}

/**
 * Returns a [Result] that contains an instance of [T] as a data payload.
 *
 * @return A [Result] the contains an instance of [T] as a data payload.
 */
public fun <T : Any> T.toResult(): Result<T> = Result.success(this)
