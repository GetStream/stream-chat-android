package io.getstream.chat.android.offline.extensions

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
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
