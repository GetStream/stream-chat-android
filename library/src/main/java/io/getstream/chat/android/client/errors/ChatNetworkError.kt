package io.getstream.chat.android.client.errors

class ChatNetworkError(
    val description: String = "",
    cause: Throwable? = null,
    val streamCode: Int = -1,
    val statusCode: Int = -1
) :
    ChatError(
        "Status code: $statusCode, with stream code: $streamCode, description: $description",
        cause
    )