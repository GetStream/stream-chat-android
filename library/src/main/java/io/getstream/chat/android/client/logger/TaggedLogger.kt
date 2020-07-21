package io.getstream.chat.android.client.logger

interface TaggedLogger {
    fun logI(message: String)

    fun logD(message: String)

    fun logW(message: String)

    fun logE(message: String)

    fun logE(throwable: Throwable)

    fun logE(message: String, throwable: Throwable)

    fun getLevel(): ChatLogLevel
}
