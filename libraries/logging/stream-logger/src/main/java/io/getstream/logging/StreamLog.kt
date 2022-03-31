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
     * @param message The function returning a message you would like logged.
     */
    public fun e(tag: String, throwable: Throwable, message: () -> String) {
        logger.log(ERROR, tag, message, throwable)
    }

    /**
     * Send a [ERROR] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun e(tag: String, message: () -> String) {
        logger.log(ERROR, tag, message)
    }

    /**
     * Send a [WARN] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun w(tag: String, message: () -> String) {
        logger.log(WARN, tag, message)
    }

    /**
     * Send a [INFO] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun i(tag: String, message: () -> String) {
        logger.log(INFO, tag, message)
    }

    /**
     * Send a [DEBUG] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun d(tag: String, message: () -> String) {
        logger.log(DEBUG, tag, message)
    }

    /**
     * Send a [VERBOSE] log message.
     *
     * @param tag Used to identify the source of a log message. It usually identifies the class or activity where the log call occurs.
     * @param message The function returning a message you would like logged.
     */
    public fun v(tag: String, message: () -> String) {
        logger.log(VERBOSE, tag, message)
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
     * @param message The function returning a message you would like logged.
     */
    public fun e(throwable: Throwable, message: () -> String)

    /**
     * Send a [ERROR] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun e(message: () -> String)

    /**
     * Send a [WARN] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun w(message: () -> String)

    /**
     * Send a [INFO] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun i(message: () -> String)

    /**
     * Send a [DEBUG] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun d(message: () -> String)

    /**
     * Send a [VERBOSE] log message.
     *
     * @param message The function returning a message you would like logged.
     */
    public fun v(message: () -> String)
}
