/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.assertInstanceOf

internal fun <T : Any> verifyNetworkError(result: Result<T>, statusCode: Int) {
    assertInstanceOf<Result.Failure>(result)
    val error = result.value
    assertInstanceOf<Error.NetworkError>(error)
    assertEquals(statusCode, error.statusCode)
}

internal fun <T : Any> verifyThrowableError(result: Result<T>, message: String, cause: Class<out Throwable>) {
    assertInstanceOf<Result.Failure>(result)
    val error = result.value
    assertInstanceOf<Error.ThrowableError>(error)
    assertEquals(message, error.message)
    assertInstanceOf(cause, error.cause)
}

internal fun <T : Any> verifyGenericError(result: Result<T>, message: String) {
    assertInstanceOf<Result.Failure>(result)
    val error = result.value
    assertInstanceOf<Error.GenericError>(error)
    assertEquals(message, error.message)
}

internal fun <T : Any> verifySuccess(result: Result<T>, equalsTo: T) {
    assertInstanceOf<Result.Success<T>>(result)
    assertEquals(equalsTo, result.value)
}
