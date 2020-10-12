package io.getstream.chat.android.client.logger

import io.getstream.chat.android.client.errors.ChatError

internal interface TaggedLogger {
    fun logI(message: String)

    fun logD(message: String)

    fun logW(message: String)

    fun logE(message: String)

    fun logE(throwable: Throwable)

    fun logE(chatError: ChatError)

    fun logE(message: String, throwable: Throwable)

    fun logE(message: String, chatError: ChatError)

    fun getLevel(): ChatLogLevel
}
