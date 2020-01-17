package io.getstream.chat.android.core.poc.library.errors

class SocketChatError(message: String, cause: Throwable? = null) : ChatError(message, cause)