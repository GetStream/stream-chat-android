package io.getstream.chat.android.client.errors

open class ChatError(
    val message: String?,
    val cause: Throwable? = null
) {
    constructor(throwable: Throwable?) : this(null, throwable)
}
