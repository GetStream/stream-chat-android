package io.getstream.chat.android.client.logger

import io.getstream.chat.android.client.errors.ChatError

public interface ChatLogger {

    public fun logI(tag: Any, message: String)

    public fun logD(tag: Any, message: String)

    public fun logW(tag: Any, message: String)

    public fun logE(tag: Any, message: String)

    public fun logE(tag: Any, throwable: Throwable)

    public fun logE(tag: Any, message: String, throwable: Throwable)

    public fun logE(tag: Any, chatError: ChatError)

    public fun logE(tag: Any, message: String, chatError: ChatError)

    public fun getLevel(): ChatLogLevel

    public data class Config(val level: ChatLogLevel, val handler: ChatLoggerHandler?)

    public class Builder(config: Config) {

        private var level = config.level
        private var handler: ChatLoggerHandler? = config.handler

        public fun level(level: ChatLogLevel): Builder {
            this.level = level
            return this
        }

        public fun handler(handler: ChatLoggerHandler): Builder {
            this.handler = handler
            return this
        }

        public fun build(): ChatLogger {
            val result = ChatLoggerImpl(level, handler)
            instance = result
            return result
        }
    }

    public companion object {
        public var instance: ChatLogger = ChatSilentLogger()

        public fun get(tag: Any): TaggedLogger {
            return TaggedLoggerImpl(tag, instance)
        }
    }
}
