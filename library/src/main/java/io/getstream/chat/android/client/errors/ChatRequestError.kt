package io.getstream.chat.android.client.errors

import java.io.IOException

/**
 * Used to interrupt okhttp request.
 * Only descendant [IOException] of can propagate call execution
 */
class ChatRequestError(
    message: String,
    val streamCode: Int,
    val statusCode:Int,
    cause: Throwable? = null) : IOException(message, cause)