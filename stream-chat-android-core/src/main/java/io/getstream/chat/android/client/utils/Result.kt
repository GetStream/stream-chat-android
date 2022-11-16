/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.errors.ChatError

/**
 *  A class which encapsulates a successful outcome with a value of type [A] or a failure with [ChatError].
 */
public sealed class Result<out A : Any> {

    /**
     * Checks if the result is a [Success].
     */
    public val isSuccess: Boolean
        inline get() = this is Success

    /**
     * Check if the result is a [Failure].
     */
    public val isFailure: Boolean
        inline get() = this is Failure

    /**
     * Returns the encapsulated value if this instance represents [Success] [Result.isSuccess] or `null`
     * if it is [Failure] [Result.isFailure].
     */
    public fun getOrNull(): A? = when (this) {
        is Success -> value
        is Failure -> null
    }

    /**
     * Returns the encapsulated value if this instance represents [Success] [Result.isSuccess]
     * or throws the [IllegalStateException] exception if it is [Failure] [Result.isFailure].
     */
    @Throws(IllegalStateException::class)
    public fun getOrThrow(): A = when (this) {
        is Success -> value
        is Failure -> throw IllegalStateException("The Success::value cannot be accessed as the Result is a Failure.")
    }

    /**
     * Returns the encapsulated [ChatError] if this instance represents [Failure] [isFailure] or `null`
     * if it is [Success] [isSuccess].
     */
    public fun chatErrorOrNull(): ChatError? = when (this) {
        is Success -> null
        is Failure -> value
    }

    /**
     * Represents successful result.
     *
     * @param value The [A] data associated with the result.
     */
    public data class Success<out A : Any>(val value: A) : Result<A>()

    /**
     * Represents failed result.
     *
     * @param value The [ChatError] associated with the result.
     */
    public data class Failure(val value: ChatError) : Result<Nothing>()

    /**
     * Returns a transformed [Result] of applying the given [f] function if the [Result]
     * contains a successful data payload.
     * Returns an original [Result] if the [Result] contains an error payload.
     *
     * @param f A lambda for mapping [Result] of [A] to [Result] of [C].
     *
     * @return A transformed instance of the [Result] or the original instance of the [Result].
     */
    public inline fun <C : Any> map(f: (A) -> C): Result<C> = flatMap { Success(f(it)) }

    /**
     * Returns a transformed [Result] of applying the given suspending [f] function if the [Result]
     * contains a successful data payload.
     * Returns an original [Result] if the [Result] contains an error payload.
     *
     * @param f A suspending lambda for mapping [Result] of [A] to [Result] of [C].
     *
     * @return A transformed instance of the [Result] or the original instance of the [Result].
     */
    @JvmSynthetic
    public suspend inline fun <C : Any> mapSuspend(crossinline f: suspend (A) -> C): Result<C> =
        flatMap { Success(f(it)) }

    /**
     * Returns a [Result] of [Unit] from any type of a [Result].
     *
     * @return [Result] of [Unit].
     */
    public fun toUnitResult(): Result<Unit> = map {}

    /**
     * Runs the [successSideEffect] lambda function if the [Result] contains a successful data payload.
     *
     * @param successSideEffect A lambda that receives the successful data payload.
     *
     * @return The original instance of the [Result].
     */
    public inline fun onSuccess(
        crossinline successSideEffect: (A) -> Unit,
    ): Result<A> =
        also {
            when (it) {
                is Success -> successSideEffect(it.value)
                is Failure -> Unit
            }
        }

    /**
     * Runs the [errorSideEffect] lambda function if the [Result] contains an error payload.
     *
     * @param errorSideEffect A lambda that receives the [ChatError] payload.
     *
     * @return The original instance of the [Result].
     */
    public inline fun onError(
        crossinline errorSideEffect: (ChatError) -> Unit,
    ): Result<A> =
        also {
            when (it) {
                is Success -> Unit
                is Failure -> errorSideEffect(it.value)
            }
        }
}

/**
 * Returns a transformed [Result] from results of the [f] if the [Result] contains a successful data payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param f A lambda that returns [Result] of [C].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
public inline fun <A : Any, C : Any> Result<A>.flatMap(f: (A) -> Result<C>): Result<C> {
    return when (this) {
        is Result.Success -> f(this.value)
        is Result.Failure -> this
    }
}

/**
 * Returns a transformed [Result] from results of the suspending [f] if the [Result] contains a successful data
 * payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param f A suspending lambda that returns [Result] of [C].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public suspend inline fun <A : Any, C : Any> Result<A>.flatMapSuspend(
    crossinline f: suspend (A) -> Result<C>,
): Result<C> {
    return when (this) {
        is Result.Success -> f(this.value)
        is Result.Failure -> this
    }
}

/**
 * Runs the suspending [successSideEffect] lambda function if the [Result] contains a successful data payload.
 *
 * @param successSideEffect A suspending lambda that receives the successful data payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public suspend inline fun <A : Any> Result<A>.onSuccessSuspend(
    crossinline successSideEffect: suspend (A) -> Unit,
): Result<A> =
    also {
        when (it) {
            is Result.Success -> successSideEffect(it.value)
            is Result.Failure -> Unit
        }
    }

/**
 * Runs the suspending [errorSideEffect] lambda function if the [Result] contains an error payload.
 *
 * @param errorSideEffect A suspending lambda that receives the [ChatError] payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public suspend inline fun <A : Any> Result<A>.onErrorSuspend(
    crossinline errorSideEffect: suspend (ChatError) -> Unit,
): Result<A> =
    also {
        when (it) {
            is Result.Success -> Unit
            is Result.Failure -> errorSideEffect(it.value)
        }
    }

/**
 * Recovers the error payload by applying the given [errorMapper] function if the [Result]
 * contains an error payload.
 *
 * @param errorMapper A lambda that receives [ChatError] and transforms it as a payload [A].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public fun <A : Any> Result<A>.recover(errorMapper: (ChatError) -> A): Result.Success<A> {
    return when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Success(errorMapper(value))
    }
}

/**
 * Recovers the error payload by applying the given suspending [errorMapper] function if the [Result]
 * contains an error payload.
 *
 * @param errorMapper A suspending lambda that receives [ChatError] and transforms it as a payload [A].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public suspend inline fun <A : Any> Result<A>.recoverSuspend(
    crossinline errorMapper: suspend (ChatError) -> A,
): Result.Success<A> {
    return when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Success(errorMapper(value))
    }
}
