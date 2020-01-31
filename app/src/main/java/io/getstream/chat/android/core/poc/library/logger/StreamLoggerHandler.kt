package io.getstream.chat.android.core.poc.library.logger

interface StreamLoggerHandler {
    fun logT(throwable: Throwable)

    fun logT(className: String, throwable: Throwable)

    fun logI(className: String, message: String)

    fun logD(className: String, message: String)

    fun logW(className: String, message: String)

    fun logE(className: String, message: String)
}