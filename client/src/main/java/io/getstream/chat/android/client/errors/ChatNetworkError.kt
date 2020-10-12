package io.getstream.chat.android.client.errors

internal class ChatNetworkError private constructor(
    val description: String,
    cause: Throwable? = null,
    val streamCode: Int,
    val statusCode: Int
) : ChatError(
    "Status code: $statusCode, with stream code: $streamCode, description: $description",
    cause
) {
    companion object {
        fun create(
            code: ChatErrorCode,
            cause: Throwable? = null,
            statusCode: Int = -1
        ): ChatNetworkError {
            return ChatNetworkError(code.description, cause, code.code, statusCode)
        }

        fun create(
            streamCode: Int,
            description: String,
            statusCode: Int,
            cause: Throwable? = null
        ): ChatNetworkError {
            return ChatNetworkError(description, cause, streamCode, statusCode)
        }
    }
}
