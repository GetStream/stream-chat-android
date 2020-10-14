package io.getstream.chat.android.client.errors

internal class ChatParsingError : Exception {
    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
