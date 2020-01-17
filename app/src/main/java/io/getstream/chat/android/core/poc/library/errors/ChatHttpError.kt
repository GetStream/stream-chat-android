package io.getstream.chat.android.core.poc.library.errors

class ChatHttpError(val statusCode: Int, message: String = "", cause: Throwable? = null) :
    ChatError("Http error with status code: $statusCode, message: $message, cause: $cause")