package io.getstream.chat.android.client.logger

import io.getstream.logging.Priority
import io.getstream.logging.Priority.ASSERT
import io.getstream.logging.Priority.DEBUG
import io.getstream.logging.Priority.ERROR
import io.getstream.logging.Priority.INFO
import io.getstream.logging.Priority.VERBOSE
import io.getstream.logging.Priority.WARN
import io.getstream.logging.StreamLogger

/**
 * A connection layer between [StreamLogger] and [ChatLoggerHandler].
 */
internal class StreamLoggerHandler(
    private val handler: ChatLoggerHandler?
) : StreamLogger {

    /**
     * Passes log messages to the specified [handler].
     */
    override fun log(priority: Priority, tag: String, message: String, throwable: Throwable?) {
        handler?.run {
            when (priority) {
                VERBOSE -> logV(tag, message)
                DEBUG -> logD(tag, message)
                INFO -> logI(tag, message)
                WARN -> logW(tag, message)
                ERROR, ASSERT -> when (throwable) {
                    null -> logE(tag, message)
                    else -> logE(tag, message, throwable)
                }
            }
        }
    }
}