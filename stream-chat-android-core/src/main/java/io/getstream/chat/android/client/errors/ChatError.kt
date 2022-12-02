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

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.StreamHandsOff
import java.net.UnknownHostException

/**
 * Represents the error in the SDK.
 */
public sealed class ChatError {

    public abstract val message: String

    /**
     * An error that only contains the message.
     *
     * @param message The message describing the error.
     */
    public data class GenericError(override val message: String) : ChatError()

    /**
     * An error that contains a message and cause.
     *
     * @param message The message describing the error.
     * @param cause The [Throwable] associated with the error.
     */
    public data class ThrowableError(override val message: String, public val cause: Throwable) : ChatError() {

        @StreamHandsOff(
            "Throwable doesn't override the equals method;" +
                " therefore, it needs custom implementation."
        )
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            return (other as? ChatError)?.let {
                message == it.message && cause.equalCause(it.extractCause())
            } ?: false
        }

        private fun Throwable?.equalCause(other: Throwable?): Boolean {
            if ((this == null && other == null) || this === other) return true
            return this?.message == other?.message && this?.cause.equalCause(other?.cause)
        }

        @StreamHandsOff(
            "Throwable doesn't override the hashCode method;" +
                " therefore, it needs custom implementation."
        )
        override fun hashCode(): Int {
            return 31 * message.hashCode() + cause.hashCode()
        }
    }

    /**
     * An error resulting from the network operation.
     *
     * @param message The message describing the error.
     * @param streamCode The code returned by the Stream backend.
     * @param statusCode HTTP status code or [UNKNOWN_STATUS_CODE] if not available.
     * @param cause The optional [Throwable] associated with the error.
     */
    public data class NetworkError(
        override val message: String,
        public val streamCode: Int,
        public val statusCode: Int = UNKNOWN_STATUS_CODE,
        public val cause: Throwable? = null,
    ) : ChatError() {

        @StreamHandsOff(
            "Throwable doesn't override the equals method;" +
                " therefore, it needs custom implementation."
        )
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            return (other as? ChatError)?.let {
                message == it.message && cause.equalCause(it.extractCause())
            } ?: false
        }

        private fun Throwable?.equalCause(other: Throwable?): Boolean {
            if ((this == null && other == null) || this === other) return true
            return this?.message == other?.message && this?.cause.equalCause(other?.cause)
        }

        @StreamHandsOff(
            "Throwable doesn't override the hashCode method;" +
                " therefore, it needs custom implementation."
        )
        override fun hashCode(): Int {
            return 31 * message.hashCode() + (cause?.hashCode() ?: 0)
        }

        @InternalStreamChatApi
        public companion object {

            /**
             * Creates [NetworkError] from [ChatErrorCode] with custom status code and optional cause.
             *
             * @param chatErrorCode The [ChatErrorCode] from which the error should be created.
             * @param statusCode HTTP status code or [UNKNOWN_STATUS_CODE] if not available.
             * @param cause The optional [Throwable] associated with the error.
             *
             * @return [NetworkError] instance.
             */
            @InternalStreamChatApi
            public fun fromChatErrorCode(
                chatErrorCode: ChatErrorCode,
                statusCode: Int = UNKNOWN_STATUS_CODE,
                cause: Throwable? = null,
            ): NetworkError {
                return NetworkError(
                    message = chatErrorCode.description,
                    streamCode = chatErrorCode.code,
                    statusCode = statusCode,
                    cause = cause,
                )
            }

            private const val UNKNOWN_STATUS_CODE = -1
        }
    }
}

private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_TIMEOUT = 408
private const val HTTP_API_ERROR = 500

/**
 * Returns true if an error is a permanent failure instead of a temporary one (broken network, 500, rate limit etc.)
 *
 * A permanent error is an error returned by Stream's API (IE a validation error on the input)
 * Any permanent error will always have a stream error code
 *
 * Temporary errors are retried. Network not being available is a common example of a temporary error.
 *
 * See the error codes here
 * https://getstream.io/chat/docs/api_errors_response/?language=js
 */
@InternalStreamChatApi
public fun ChatError.isPermanent(): Boolean {
    return if (this is ChatError.NetworkError) {
        // stream errors are mostly permanent. the exception to this are the rate limit and timeout error
        val temporaryStreamErrors = listOf(HTTP_TOO_MANY_REQUESTS, HTTP_TIMEOUT, HTTP_API_ERROR)

        when {
            statusCode in temporaryStreamErrors -> false
            cause is UnknownHostException -> false
            else -> true
        }
    } else {
        false
    }
}

/**
 * Copies the original [ChatError] objects with custom message.
 *
 * @param message The message to replace.
 *
 * @return New [ChatError] instance.
 */
@InternalStreamChatApi
public fun ChatError.copyWithMessage(message: String): ChatError {
    return when (this) {
        is ChatError.GenericError -> this.copy(message = message)
        is ChatError.NetworkError -> this.copy(message = message)
        is ChatError.ThrowableError -> this.copy(message = message)
    }
}

/**
 * Extracts the cause from [ChatError] object or null if it's not available.
 *
 * @return The [Throwable] that is the error's cause or null if not available.
 */
@InternalStreamChatApi
public fun ChatError.extractCause(): Throwable? {
    return when (this) {
        is ChatError.GenericError -> null
        is ChatError.NetworkError -> cause
        is ChatError.ThrowableError -> cause
    }
}
