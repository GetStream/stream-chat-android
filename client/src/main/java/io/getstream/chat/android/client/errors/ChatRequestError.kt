package io.getstream.chat.android.client.errors

import java.io.IOException

/**
 * Used to interrupt okhttp request.
 * Only descendant [IOException] of can propagate call execution
 */
internal class ChatRequestError(
    message: String,
    val streamCode: Int,
    val statusCode: Int,
    cause: Throwable? = null
) : IOException(message, cause) {
    override fun toString(): String {
        return "streamCode: $streamCode, statusCode: $statusCode, message: $message"
    }
}
