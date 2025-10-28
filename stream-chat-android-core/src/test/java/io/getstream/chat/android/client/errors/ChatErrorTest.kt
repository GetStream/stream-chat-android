/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.UnknownHostException

internal class ChatErrorTest {

    @Test
    fun testCreateErrorFromChatErrorCode() {
        // Given
        val chatErrorCode = ChatErrorCode.NETWORK_FAILED
        val statusCode = 404
        val cause = Throwable("Bad request")
        // When
        val error = Error.NetworkError.fromChatErrorCode(chatErrorCode, statusCode, cause)
        // Then
        error.message `should be equal to` chatErrorCode.description
        error.serverErrorCode `should be equal to` chatErrorCode.code
        error.statusCode `should be equal to` statusCode
        error.cause `should be equal to` cause
    }

    @Test
    fun testCreateErrorFromChatErrorCodeWithUnknownCause() {
        // Given
        val chatErrorCode = ChatErrorCode.NETWORK_FAILED
        // When
        val error = Error.NetworkError.fromChatErrorCode(chatErrorCode)
        // Then
        error.message `should be equal to` chatErrorCode.description
        error.serverErrorCode `should be equal to` chatErrorCode.code
        error.statusCode `should be equal to` Error.NetworkError.UNKNOWN_STATUS_CODE
        error.cause `should be equal to` null
    }

    @ParameterizedTest
    @MethodSource("isPermanentErrorArguments")
    fun testIsPermanentError(
        error: Error,
        isPermanent: Boolean,
    ) {
        error.isPermanent() `should be equal to` isPermanent
    }

    @ParameterizedTest
    @MethodSource("copyWithMessageArguments")
    fun testCopyWithMessage(
        error: Error,
        message: String,
        expected: Error,
    ) {
        error.copyWithMessage(message) `should be equal to` expected
    }

    @ParameterizedTest
    @MethodSource("extractCauseArguments")
    fun testExtractCause(
        error: Error,
        expected: Throwable?,
    ) {
        error.extractCause() `should be equal to` expected
    }

    companion object {

        @JvmStatic
        fun isPermanentErrorArguments() = listOf(
            Arguments.of(Error.GenericError("Error"), false),
            Arguments.of(Error.ThrowableError("Error", Throwable()), false),
            Arguments.of(networkError(ChatErrorCode.NETWORK_FAILED, statusCode = 408), false),
            Arguments.of(networkError(ChatErrorCode.NETWORK_FAILED, statusCode = 429), false),
            Arguments.of(networkError(ChatErrorCode.NETWORK_FAILED, 500), false),
            Arguments.of(
                networkError(ChatErrorCode.NETWORK_FAILED, statusCode = 400, cause = UnknownHostException()),
                false,
            ),
            Arguments.of(networkError(ChatErrorCode.NETWORK_FAILED, 400), true),
            Arguments.of(networkError(ChatErrorCode.PARSER_ERROR, 400), true),
            Arguments.of(networkError(ChatErrorCode.SOCKET_CLOSED, 400), true),
            Arguments.of(networkError(ChatErrorCode.SOCKET_FAILURE, 400), true),
            Arguments.of(networkError(ChatErrorCode.CANT_PARSE_CONNECTION_EVENT, 400), true),
            Arguments.of(networkError(ChatErrorCode.CANT_PARSE_EVENT, 400), true),
            Arguments.of(networkError(ChatErrorCode.INVALID_TOKEN, 400), true),
            Arguments.of(networkError(ChatErrorCode.UNDEFINED_TOKEN, 400), true),
            Arguments.of(networkError(ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT, 400), true),
            Arguments.of(networkError(ChatErrorCode.NO_ERROR_BODY, 400), true),
            Arguments.of(networkError(ChatErrorCode.VALIDATION_ERROR, 400), true),
            Arguments.of(networkError(ChatErrorCode.AUTHENTICATION_ERROR, 400), true),
            Arguments.of(networkError(ChatErrorCode.TOKEN_EXPIRED, 400), true),
            Arguments.of(networkError(ChatErrorCode.TOKEN_NOT_VALID, 400), true),
            Arguments.of(networkError(ChatErrorCode.TOKEN_DATE_INCORRECT, 400), true),
            Arguments.of(networkError(ChatErrorCode.TOKEN_SIGNATURE_INCORRECT, 400), true),
            Arguments.of(networkError(ChatErrorCode.API_KEY_NOT_FOUND, 400), true),
        )

        @JvmStatic
        fun copyWithMessageArguments() = listOf(
            Arguments.of(
                Error.GenericError("Error"),
                "New error",
                Error.GenericError("New error"),
            ),
            Arguments.of(
                Error.ThrowableError("Error", Throwable()),
                "New error",
                Error.ThrowableError("New error", Throwable()),
            ),
            Arguments.of(
                Error.NetworkError("Error", ChatErrorCode.NETWORK_FAILED.code, 400),
                "New error",
                Error.NetworkError("New error", ChatErrorCode.NETWORK_FAILED.code, 400),
            ),
        )

        @JvmStatic
        fun extractCauseArguments(): List<Arguments> {
            val cause = Throwable("Error")
            return listOf(
                Arguments.of(Error.GenericError("Error"), null),
                Arguments.of(Error.ThrowableError("Error", cause), cause),
                Arguments.of(networkError(ChatErrorCode.NETWORK_FAILED, statusCode = 400), null),
                Arguments.of(networkError(ChatErrorCode.NETWORK_FAILED, statusCode = 400, cause = cause), cause),
            )
        }

        private fun networkError(
            code: ChatErrorCode,
            statusCode: Int = 400,
            cause: Throwable? = null,
        ): Error.NetworkError = Error.NetworkError("Error", code.code, statusCode, cause)
    }
}
