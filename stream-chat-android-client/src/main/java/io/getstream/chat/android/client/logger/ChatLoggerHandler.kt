package io.getstream.chat.android.client.logger

public interface ChatLoggerHandler {
    public fun logT(throwable: Throwable)

    public fun logT(tag: Any, throwable: Throwable)

    public fun logI(tag: Any, message: String)

    public fun logD(tag: Any, message: String)

    public fun logW(tag: Any, message: String)

    public fun logE(tag: Any, message: String)

    public fun logE(tag: Any, message: String, throwable: Throwable)
}
