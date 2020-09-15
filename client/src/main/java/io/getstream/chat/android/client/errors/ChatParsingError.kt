package io.getstream.chat.android.client.errors

class ChatParsingError : ChatError {

    constructor(message: String?) : super(message)
    constructor(message: String?, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)
}
