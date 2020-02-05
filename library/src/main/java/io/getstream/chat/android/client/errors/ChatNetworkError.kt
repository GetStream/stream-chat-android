package io.getstream.chat.android.client.errors

class ChatNetworkError(
    message: String = "",
    cause: Throwable? = null,
    val streamCode: Int = -1,
    val statusCode: Int = -1
) :
    ChatError("Network error with status code: $statusCode, with stream code: $streamCode, message: $message, cause: $cause")