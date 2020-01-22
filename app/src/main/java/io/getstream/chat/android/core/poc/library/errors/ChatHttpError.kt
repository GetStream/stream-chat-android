package io.getstream.chat.android.core.poc.library.errors

class ChatHttpError(
    streamCode: Int,
    statusCode: Int,
    message: String = "",
    cause: Throwable? = null
) :
    ChatError("Http error with status code: $statusCode, with stream code: $streamCode, message: $message, cause: $cause")