package io.getstream.chat.android.client.errors

open class ChatError(
    val message: String?,
    val throwable: Throwable? = null
) {
    constructor(throwable: Throwable?) : this(null, throwable)
}
