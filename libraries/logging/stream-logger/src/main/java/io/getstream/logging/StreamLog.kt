package io.getstream.logging

import io.getstream.logging.StreamLogger.Level.DEBUG
import io.getstream.logging.StreamLogger.Level.ERROR
import io.getstream.logging.StreamLogger.Level.INFO
import io.getstream.logging.StreamLogger.Level.VERBOSE
import io.getstream.logging.StreamLogger.Level.WARN

public object StreamLog {

    private var logger: StreamLogger = SilentStreamLogger

    public fun init(
        logger: StreamLogger,
    ) {
        this.logger = logger
    }

    public fun getLogger(tag: String): TaggedLogger = TaggedLoggerImpl(tag, logger)

    public fun e(tag: String, throwable: Throwable, message: String, vararg args: Any?) {
        logger.log(ERROR, tag, throwable, message, args)
    }

    public fun e(tag: String, message: String, vararg args: Any?) {
        logger.log(WARN, tag, message, args)
    }

    public fun w(tag: String, message: String, vararg args: Any?) {
        logger.log(WARN, tag, message, args)
    }

    public fun i(tag: String, message: String, vararg args: Any?) {
        logger.log(INFO, tag, message, args)
    }

    public fun d(tag: String, message: String, vararg args: Any?) {
        logger.log(DEBUG, tag, message, args)
    }

    public fun v(tag: String, message: String, vararg args: Any?) {
        logger.log(VERBOSE, tag, message, args)
    }

    private fun StreamLogger.log(priority: Priority, tag: String, message: String, args: Array<out Any?>?) {
        log(priority, tag, null, message, args)
    }
}

public interface TaggedLogger {
    public fun e(throwable: Throwable, message: String, vararg args: Any?)
    public fun e(message: String, vararg args: Any?)
    public fun w(message: String, vararg args: Any?)
    public fun i(message: String, vararg args: Any?)
    public fun d(message: String, vararg args: Any?)
    public fun v(message: String, vararg args: Any?)
}
