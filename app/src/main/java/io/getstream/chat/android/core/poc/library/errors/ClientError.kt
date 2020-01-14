package io.getstream.chat.android.core.poc.library.errors

class ClientError(code: Int, message: String, cause: Throwable? = null) :
    RuntimeException(message, cause)