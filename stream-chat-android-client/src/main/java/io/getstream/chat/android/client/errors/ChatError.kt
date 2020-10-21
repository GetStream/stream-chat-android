package io.getstream.chat.android.client.errors

public open class ChatError(
    public val message: String? = null,
    public val cause: Throwable? = null
)
