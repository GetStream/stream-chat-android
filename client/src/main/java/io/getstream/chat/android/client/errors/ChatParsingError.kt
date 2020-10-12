package io.getstream.chat.android.client.errors

public class ChatParsingError : Exception {
    public constructor(message: String?) : super(message)
    public constructor(message: String?, cause: Throwable?) : super(message, cause)
    public constructor(cause: Throwable?) : super(null, cause)
}
