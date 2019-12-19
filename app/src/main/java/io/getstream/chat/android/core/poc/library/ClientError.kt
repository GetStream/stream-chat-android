package io.getstream.chat.android.core.poc.library

class ClientError(code: Int, message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)