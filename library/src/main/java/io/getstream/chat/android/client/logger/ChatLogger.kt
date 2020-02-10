package io.getstream.chat.android.client.logger

interface ChatLogger {
    fun logT(throwable: Throwable)

    fun logT(tag: Any, throwable: Throwable)

    fun logI(tag: Any, message: String)

    fun logD(tag: Any, message: String)

    fun logW(tag: Any, message: String)

    fun logE(tag: Any, message: String)
}