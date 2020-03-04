package io.getstream.chat.android.client.logger

interface ChatLogger {
    fun logT(throwable: Throwable)

    fun logT(tag: Any, throwable: Throwable)

    fun logI(tag: Any, message: String)

    fun logD(tag: Any, message: String)

    fun logW(tag: Any, message: String)

    fun logE(tag: Any, message: String)

    class Builder {

        private var loggingLevel = ChatLogLevel.NOTHING
        private var loggingHandler: ChatLoggerHandler? = null

        fun level(level: ChatLogLevel): Builder {
            loggingLevel = level
            return this
        }

        fun handler(handler: ChatLoggerHandler): Builder {
            loggingHandler = handler
            return this
        }

        fun build(): ChatLogger {
            val result = ChatLoggerImpl(loggingLevel, loggingHandler)
            instance = result
            return result
        }
    }

    companion object {
        var instance: ChatLogger = ChatSilentLogger()
    }
}