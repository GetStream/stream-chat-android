package io.getstream.chat.android.client.errors

public class ChatNetworkError private constructor(
    public val description: String,
    cause: Throwable? = null,
    public val streamCode: Int,
    public val statusCode: Int
) :
    ChatError(
        "Status code: $statusCode, with stream code: $streamCode, description: $description",
        cause
    ) {
    public companion object {
        public fun create(
            code: ChatErrorCode,
            cause: Throwable? = null,
            statusCode: Int = -1
        ): ChatNetworkError {
            return ChatNetworkError(code.description, cause, code.code, statusCode)
        }

        public fun create(
            streamCode: Int,
            description: String,
            statusCode: Int,
            cause: Throwable? = null
        ): ChatNetworkError {
            return ChatNetworkError(description, cause, streamCode, statusCode)
        }
    }
}
