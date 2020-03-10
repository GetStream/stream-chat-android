package io.getstream.chat.android.client.logger

internal class TaggedLoggerImpl(val tag: Any, val logger: ChatLogger) : TaggedLogger {


    override fun logI(message: String) {
        logger.logI(tag, message)
    }

    override fun logD(message: String) {
        logger.logD(tag, message)
    }

    override fun logW(message: String) {
        logger.logW(tag, message)
    }

    override fun logE(message: String) {
        logger.logE(tag, message)
    }

    override fun logE(throwable: Throwable) {
        logger.logE(tag, throwable)
    }

    override fun logE(message: String, throwable: Throwable) {
        logger.logE(tag, message, throwable)
    }

    override fun getLevel(): ChatLogLevel {
        return logger.getLevel()
    }
}