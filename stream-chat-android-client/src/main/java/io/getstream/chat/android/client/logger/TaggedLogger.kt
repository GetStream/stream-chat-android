package io.getstream.chat.android.client.logger

import io.getstream.chat.android.client.errors.ChatError

public interface TaggedLogger {
    public fun logI(message: String)

    public fun logD(message: String)

    public fun logW(message: String)

    public fun logE(message: String)

    public fun logE(throwable: Throwable)

    public fun logE(chatError: ChatError)

    public fun logE(message: String, throwable: Throwable)

    public fun logE(message: String, chatError: ChatError)

    public fun getLevel(): ChatLogLevel
}
