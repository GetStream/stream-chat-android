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

import io.getstream.result.Error
import io.getstream.result.Result
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf

internal fun <T : Any> verifyNetworkError(result: Result<T>, statusCode: Int) {
    result shouldBeInstanceOf Result.Failure::class
    (result as Result.Failure).value.shouldBeInstanceOf<Error.NetworkError>()

    val error = result.value as Error.NetworkError
    error.statusCode shouldBeEqualTo statusCode
}

internal fun <T : Any> verifyThrowableError(result: Result<T>, message: String, cause: Class<out Throwable>) {
    result shouldBeInstanceOf Result.Failure::class
    (result as Result.Failure).value.shouldBeInstanceOf<Error.ThrowableError>()

    val error = result.value as Error.ThrowableError
    error.message shouldBeEqualTo message
    error.cause shouldBeInstanceOf cause
}

internal fun <T : Any> verifyGenericError(result: Result<T>, message: String) {
    result shouldBeInstanceOf Result.Failure::class
    (result as Result.Failure).value.shouldBeInstanceOf<Error.GenericError>()

    val error = result.value as Error.GenericError
    error.message shouldBeEqualTo message
}

internal fun <T : Any> verifySuccess(result: Result<T>, equalsTo: T) {
    result shouldBeInstanceOf Result.Success::class
    (result as Result.Success).value shouldBeEqualTo equalsTo
}
