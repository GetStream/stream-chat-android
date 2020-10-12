package io.getstream.chat.android.client.errors

public open class ChatError(
    public val message: String?,
    public val cause: Throwable? = null
) {
    public constructor(throwable: Throwable?) : this(null, throwable)
}
