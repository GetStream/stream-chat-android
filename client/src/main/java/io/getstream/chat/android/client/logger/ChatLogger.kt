package io.getstream.chat.android.client.logger

import io.getstream.chat.android.client.errors.ChatError

internal interface ChatLogger {

    fun logI(tag: Any, message: String)

    fun logD(tag: Any, message: String)

    fun logW(tag: Any, message: String)

    fun logE(tag: Any, message: String)

    fun logE(tag: Any, throwable: Throwable)

    fun logE(tag: Any, message: String, throwable: Throwable)

    fun logE(tag: Any, chatError: ChatError)

    fun logE(tag: Any, message: String, chatError: ChatError)

    fun getLevel(): ChatLogLevel

    data class Config(val level: ChatLogLevel, val handler: ChatLoggerHandler?)

    class Builder(config: Config) {

        private var level = config.level
        private var handler: ChatLoggerHandler? = config.handler

        fun level(level: ChatLogLevel): Builder {
            this.level = level
            return this
        }

        fun handler(handler: ChatLoggerHandler): Builder {
            this.handler = handler
            return this
        }

        fun build(): ChatLogger {
            val result = ChatLoggerImpl(level, handler)
            instance = result
            return result
        }
    }

    companion object {
        var instance: ChatLogger = ChatSilentLogger()

        fun get(tag: Any): TaggedLogger {
            return TaggedLoggerImpl(tag, instance)
        }
    }
}
