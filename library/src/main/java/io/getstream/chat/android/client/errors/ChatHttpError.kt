package io.getstream.chat.android.client.errors

class ChatHttpError(
    val streamCode: Int,
    val statusCode: Int,
    message: String = "",
    cause: Throwable? = null
) :
    ChatError("Http error with status code: $statusCode, with stream code: $streamCode, message: $message, cause: $cause")