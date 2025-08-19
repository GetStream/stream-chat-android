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

package io.getstream.chat.android.client.errors

private const val NETWORK_FAILED_ERROR_CODE = 1000
private const val PARSER_ERROR_ERROR_CODE = 1001
private const val SOCKET_CLOSED_ERROR_CODE = 1002
private const val SOCKET_FAILURE_ERROR_CODE = 1003
private const val CANT_PARSE_CONNECTION_EVENT_ERROR_CODE = 1004
private const val CANT_PARSE_EVENT_ERROR_CODE = 1005
private const val INVALID_TOKEN_ERROR_CODE = 1006
private const val UNDEFINED_TOKEN_ERROR_CODE = 1007
private const val UNABLE_TO_PARSE_SOCKET_EVENT_ERROR_CODE = 1008
private const val NO_ERROR_BODY_ERROR_CODE = 1009
private const val VALIDATION_ERROR_ERROR_CODE = 4
private const val AUTHENTICATION_ERROR_CODE = 5
private const val DUPLICATE_USERNAME_ERROR_CODE = 6
private const val TOKEN_EXPIRED_ERROR_CODE = 40
private const val TOKEN_NOT_VALID_ERROR_CODE = 41
private const val TOKEN_DATE_INCORRECT_ERROR_CODE = 42
private const val TOKEN_SIGNATURE_INCORRECT_ERROR_CODE = 43
private const val API_KEY_NOT_FOUND_ERROR_CODE = 2

/**
 * Enumerable that associates code returned by the Stream backend with description.
 *
 * @param code The code returned by the backend.
 * @param description Error's description.
 */
public enum class ChatErrorCode(public val code: Int, public val description: String) {

    // client error codes
    NETWORK_FAILED(NETWORK_FAILED_ERROR_CODE, "Response is failed. See cause"),
    PARSER_ERROR(PARSER_ERROR_ERROR_CODE, "Unable to parse error"),
    SOCKET_CLOSED(SOCKET_CLOSED_ERROR_CODE, "Server closed connection"),
    SOCKET_FAILURE(SOCKET_FAILURE_ERROR_CODE, "See stack trace in logs. Intercept error in error handler of setUser"),
    CANT_PARSE_CONNECTION_EVENT(CANT_PARSE_CONNECTION_EVENT_ERROR_CODE, "Unable to parse connection event"),
    CANT_PARSE_EVENT(CANT_PARSE_EVENT_ERROR_CODE, "Unable to parse event"),
    INVALID_TOKEN(INVALID_TOKEN_ERROR_CODE, "Invalid token"),
    UNDEFINED_TOKEN(UNDEFINED_TOKEN_ERROR_CODE, "No defined token. Check if client.setUser was called and finished"),
    UNABLE_TO_PARSE_SOCKET_EVENT(
        UNABLE_TO_PARSE_SOCKET_EVENT_ERROR_CODE,
        "Socket event payload either invalid or null",
    ),
    NO_ERROR_BODY(NO_ERROR_BODY_ERROR_CODE, "No error body. See http status code"),

    // server error codes
    VALIDATION_ERROR(VALIDATION_ERROR_ERROR_CODE, "Validation error, check your credentials"),
    AUTHENTICATION_ERROR(AUTHENTICATION_ERROR_CODE, "Unauthenticated, problem with authentication"),
    DUPLICATE_USERNAME_ERROR(DUPLICATE_USERNAME_ERROR_CODE, "Username(s) already exists."),
    TOKEN_EXPIRED(TOKEN_EXPIRED_ERROR_CODE, "Token expired, new one must be requested."),
    TOKEN_NOT_VALID(TOKEN_NOT_VALID_ERROR_CODE, "Unauthenticated, token not valid yet"),
    TOKEN_DATE_INCORRECT(TOKEN_DATE_INCORRECT_ERROR_CODE, "Unauthenticated, token date incorrect"),
    TOKEN_SIGNATURE_INCORRECT(TOKEN_SIGNATURE_INCORRECT_ERROR_CODE, "Unauthenticated, token signature invalid"),
    API_KEY_NOT_FOUND(API_KEY_NOT_FOUND_ERROR_CODE, "Api key is not found, verify it if it's correct or was created."),
    ;

    public companion object {
        private val authenticationErrors = setOf(
            AUTHENTICATION_ERROR_CODE,
            TOKEN_EXPIRED_ERROR_CODE,
            TOKEN_NOT_VALID_ERROR_CODE,
            TOKEN_DATE_INCORRECT_ERROR_CODE,
            TOKEN_SIGNATURE_INCORRECT_ERROR_CODE,
        )

        /**
         * Checks if the error represented by the code is an authentication error.
         *
         * @see authenticationErrors
         *
         * @param code The code returned by the Stream backend.
         */
        public fun isAuthenticationError(code: Int): Boolean = authenticationErrors.contains(code)
    }
}
