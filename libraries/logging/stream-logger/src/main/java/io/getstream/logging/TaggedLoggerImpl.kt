package io.getstream.logging

internal class TaggedLoggerImpl(
    private val tag: String,
    private val delegate: StreamLogger,
) : TaggedLogger {

    override fun e(throwable: Throwable, message: () -> String) {
        delegate.log(StreamLogger.ERROR, tag, message, throwable)
    }

    override fun e(message: () -> String) {
        delegate.log(StreamLogger.ERROR, tag, message)
    }

    override fun w(message: () -> String) {
        delegate.log(StreamLogger.WARN, tag, message)
    }

    override fun i(message: () -> String) {
        delegate.log(StreamLogger.INFO, tag, message)
    }

    override fun d(message: () -> String) {
        delegate.log(StreamLogger.DEBUG, tag, message)
    }

    override fun v(message: () -> String) {
        delegate.log(StreamLogger.VERBOSE, tag, message)
    }
}
