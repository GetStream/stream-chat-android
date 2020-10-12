package io.getstream.chat.android.client.errors

import java.io.IOException

/**
 * Used to interrupt okhttp request.
 * Only descendant [IOException] of can propagate call execution
 */
public class ChatRequestError(
    message: String,
    public val streamCode: Int,
    public val statusCode: Int,
    cause: Throwable? = null
) : IOException(message, cause) {
    override fun toString(): String {
        return "streamCode: $streamCode, statusCode: $statusCode, message: $message"
    }
}
