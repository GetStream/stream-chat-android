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

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

internal class ChatErrorCodeTest {

    @ParameterizedTest
    @MethodSource("isAuthenticationErrorArguments")
    fun testIsAuthenticationError(
        code: Int,
        isAuthenticationError: Boolean,
    ) {
        ChatErrorCode.isAuthenticationError(code) `should be equal to` isAuthenticationError
    }

    companion object {

        @JvmStatic
        fun isAuthenticationErrorArguments() = listOf(
            arrayOf(ChatErrorCode.NETWORK_FAILED.code, false),
            arrayOf(ChatErrorCode.PARSER_ERROR.code, false),
            arrayOf(ChatErrorCode.SOCKET_CLOSED.code, false),
            arrayOf(ChatErrorCode.SOCKET_FAILURE.code, false),
            arrayOf(ChatErrorCode.CANT_PARSE_CONNECTION_EVENT.code, false),
            arrayOf(ChatErrorCode.CANT_PARSE_EVENT.code, false),
            arrayOf(ChatErrorCode.INVALID_TOKEN.code, false),
            arrayOf(ChatErrorCode.UNDEFINED_TOKEN.code, false),
            arrayOf(ChatErrorCode.UNABLE_TO_PARSE_SOCKET_EVENT.code, false),
            arrayOf(ChatErrorCode.NO_ERROR_BODY.code, false),
            arrayOf(ChatErrorCode.VALIDATION_ERROR.code, false),
            arrayOf(ChatErrorCode.AUTHENTICATION_ERROR.code, true),
            arrayOf(ChatErrorCode.TOKEN_EXPIRED.code, true),
            arrayOf(ChatErrorCode.TOKEN_NOT_VALID.code, true),
            arrayOf(ChatErrorCode.TOKEN_DATE_INCORRECT.code, true),
            arrayOf(ChatErrorCode.TOKEN_SIGNATURE_INCORRECT.code, true),
            arrayOf(ChatErrorCode.API_KEY_NOT_FOUND.code, false),
        )
    }
}
