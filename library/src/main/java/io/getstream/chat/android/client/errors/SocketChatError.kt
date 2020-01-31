package io.getstream.chat.android.client.errors

class SocketChatError(message: String, cause: Throwable? = null) : ChatError(message, cause)