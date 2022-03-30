package io.getstream.logging

import io.getstream.logging.StreamLogger.Level.DEBUG
import io.getstream.logging.StreamLogger.Level.ERROR
import io.getstream.logging.StreamLogger.Level.INFO
import io.getstream.logging.StreamLogger.Level.VERBOSE
import io.getstream.logging.StreamLogger.Level.WARN

public object StreamLog {

    private var logger: StreamLogger = SilentStreamLogger

    /**
     * Sets custom [StreamLogger] implementation.
     */
    public fun init(
        logger: StreamLogger,
    ) {
        this.logger = logger
    }

    /**
     * Returns a tagged logger.
     *
     * @return [TaggedLogger] Tagged logger.
     */
    public fun getLogger(tag: String): TaggedLogger = TaggedLoggerImpl(tag, logger)

    /**
     * Send a [ERROR] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param throwable An exception to log.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun e(tag: String, throwable: Throwable, message: String, vararg args: Any?) {
        logger.log(ERROR, tag, throwable, message, args)
    }

    /**
     * Send a [ERROR] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun e(tag: String, message: String, vararg args: Any?) {
        logger.log(WARN, tag, message, args)
    }

    /**
     * Send a [WARN] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun w(tag: String, message: String, vararg args: Any?) {
        logger.log(WARN, tag, message, args)
    }

    /**
     * Send a [INFO] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun i(tag: String, message: String, vararg args: Any?) {
        logger.log(INFO, tag, message, args)
    }

    /**
     * Send a [DEBUG] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun d(tag: String, message: String, vararg args: Any?) {
        logger.log(DEBUG, tag, message, args)
    }

    /**
     * Send a [VERBOSE] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun v(tag: String, message: String, vararg args: Any?) {
        logger.log(VERBOSE, tag, message, args)
    }

    private fun StreamLogger.log(priority: Priority, tag: String, message: String, args: Array<out Any?>?) {
        log(priority, tag, null, message, args)
    }
}

/**
 * Represents a tagged logger.
 */
public interface TaggedLogger {

    /**
     * Send a [ERROR] log message.
     *
     * @param throwable An exception to log.
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun e(throwable: Throwable, message: String, vararg args: Any?)

    /**
     * Send a [ERROR] log message.
     *
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun e(message: String, vararg args: Any?)

    /**
     * Send a [WARN] log message.
     *
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun w(message: String, vararg args: Any?)

    /**
     * Send a [INFO] log message.
     *
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun i(message: String, vararg args: Any?)

    /**
     * Send a [DEBUG] log message.
     *
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun d(message: String, vararg args: Any?)

    /**
     * Send a [VERBOSE] log message.
     *
     * @param message The format string you would like logged.
     * @param args Arguments referenced by the format specifiers in the format string.
     */
    public fun v(message: String, vararg args: Any?)
}
