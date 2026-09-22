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

package io.getstream.chat.android.client.errors

import io.getstream.result.Error
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class ErrorExtensionsTest {

    @ParameterizedTest
    @MethodSource("duplicateMessageErrorArguments")
    fun `isDuplicateMessageError should return expected result`(error: Error, expected: Boolean) {
        error.isDuplicateMessageError() shouldBeEqualTo expected
    }

    companion object {
        @JvmStatic
        fun duplicateMessageErrorArguments() = listOf(
            Arguments.of(alreadyExistsError(), true),
            Arguments.of(
                alreadyExistsError(
                    message = "SendMessage failed with error: \"a message with ID abc already exists\"",
                ),
                true,
            ),
            Arguments.of(alreadyExistsError(message = "channel members are limited to 100"), false),
            Arguments.of(alreadyExistsError(message = "poll with ID `p1` already exists"), false),
            Arguments.of(alreadyExistsError(message = "vote already exists for user `u1` on poll `p1`"), false),
            Arguments.of(alreadyExistsError(code = ChatErrorCode.AUTHENTICATION_ERROR.code), false),
            Arguments.of(Error.GenericError("a message with ID abc already exists"), false),
        )

        private fun alreadyExistsError(
            code: Int = ChatErrorCode.VALIDATION_ERROR.code,
            message: String = "a message with ID abc already exists",
        ): Error.NetworkError = Error.NetworkError(message, code, 400)
    }
}
