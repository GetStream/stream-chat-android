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
 
package io.getstream.chat.android.client.extensions

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.net.UnknownHostException

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
    return if (this is ChatNetworkError) {
        val networkError: ChatNetworkError = this
        // stream errors are mostly permanent. the exception to this are the rate limit and timeout error
        val temporaryStreamErrors = listOf(HTTP_TOO_MANY_REQUESTS, HTTP_TIMEOUT, HTTP_API_ERROR)

        when {
            networkError.statusCode in temporaryStreamErrors -> false

            networkError.cause is UnknownHostException -> false

            else -> true
        }
    } else {
        false
    }
}
