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

package io.getstream.chat.android.client

import io.getstream.chat.android.client.errors.ChatError

/**
 *  A class which encapsulates a successful outcome with a value of type [A] or a failure with [B].
 */
public sealed class Result<out A : Any, out B : ChatError> {

    /**
     * Represents successful result.
     *
     * @param value The [A] data associated with the result.
     */
    public data class Success<out A : Any>(val value: A) : Result<A, Nothing>()

    /**
     * Represents failed result.
     *
     * @param value The [ChatError] associated with the result.
     */
    public data class Failure<out B : ChatError>(val value: B) : Result<Nothing, B>()

    /**
     * Returns a transformed [Result] of applying the given [f] function if the [Result]
     * contains a successful data payload.
     * Returns an original [Result] if the [Result] contains an error payload.
     *
     * @param f A lambda for mapping [Result] of [A] to [Result] of [C].
     *
     * @return A transformed instance of the [Result] or the original instance of the [Result].
     */
    @JvmSynthetic
    public inline fun <C : Any> map(f: (A) -> C): Result<C, B> = flatMap { Success(f(it)) }

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
    public suspend inline fun <C : Any> mapSuspend(crossinline f: suspend (A) -> C): Result<C, B> =
        flatMap { Success(f(it)) }

    /**
     * Returns a [Result] of [Unit] from any type of a [Result].
     *
     * @return [Result] of [Unit].
     */
    public fun toUnitResult(): Result<Unit, B> = map {}
}

/**
 * Returns a transformed [Result] from results of the [f] if the [Result] contains a successful data payload.
 * Returns an original [Result] if the [Result] contains an error payload.
 *
 * @param f A lambda that returns [Result] of [C].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public inline fun <A : Any, B : ChatError, C : Any> Result<A, B>.flatMap(f: (A) -> Result<C, B>): Result<C, B> {
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
public suspend inline fun <A : Any, B : ChatError, C : Any> Result<A, B>.flatMapSuspend(
    crossinline f: suspend (A) -> Result<C, B>,
): Result<C, B> {
    return when (this) {
        is Result.Success -> f(this.value)
        is Result.Failure -> this
    }
}

/**
 * Runs the [successSideEffect] lambda function if the [Result] contains a successful data payload.
 *
 * @param successSideEffect A lambda that receives the successful data payload.
 *
 * @return The original instance of the [Result].
 */
@JvmSynthetic
public inline fun <A : Any, B : ChatError> Result<A, B>.onSuccess(
    crossinline successSideEffect: (A) -> Unit,
): Result<A, B> =
    also {
        when (it) {
            is Result.Success -> successSideEffect(it.value)
            is Result.Failure -> Unit
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
public suspend inline fun <A : Any, B : ChatError> Result<A, B>.onSuccessSuspend(
    crossinline successSideEffect: suspend (A) -> Unit,
): Result<A, B> =
    also {
        when (it) {
            is Result.Success -> successSideEffect(it.value)
            is Result.Failure -> Unit
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
public inline fun <A : Any, B : ChatError> Result<A, B>.onError(
    crossinline errorSideEffect: (B) -> Unit,
): Result<A, B> =
    also {
        when (it) {
            is Result.Success -> Unit
            is Result.Failure -> errorSideEffect(it.value)
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
public suspend inline fun <A : Any, B : ChatError> Result<A, B>.onErrorSuspend(
    crossinline errorSideEffect: suspend (B) -> Unit,
): Result<A, B> =
    apply {
        when (it) {
            is Result.Success -> Unit
            is Result.Failure -> errorSideEffect(it.value)
        }
    }

/**
 * Recovers the error payload by applying the given [errorMapper] function if the [Result]
 * contains an error payload.
 *
 * @param errorMapper A lambda that receives [B] and transforms it as a payload [A].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public fun <A : Any, B : ChatError> Result<A, B>.recover(errorMapper: (B) -> A): Result<A, B> {
    return when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Success(errorMapper(value))
    }
}

/**
 * Recovers the error payload by applying the given suspending [errorMapper] function if the [Result]
 * contains an error payload.
 *
 * @param errorMapper A suspending lambda that receives [B] and transforms it as a payload [A].
 *
 * @return A transformed instance of the [Result] or the original instance of the [Result].
 */
@JvmSynthetic
public suspend inline fun <A : Any, B : ChatError> Result<A, B>.recover(
    crossinline errorMapper: suspend (B) -> A,
): Result<A, B> {
    return when (this) {
        is Result.Success -> this
        is Result.Failure -> Result.Success(errorMapper(value))
    }
}
