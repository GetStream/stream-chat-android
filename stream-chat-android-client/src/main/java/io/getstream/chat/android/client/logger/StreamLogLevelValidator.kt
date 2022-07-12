package io.getstream.chat.android.client.logger

import io.getstream.logging.IsLoggableValidator
import io.getstream.logging.Priority

/**
 * Validates if a message can be logged in accordance with the provided [logLevel].
 *
 * @see ChatLogLevel
 */
internal class StreamLogLevelValidator(
    private val logLevel: ChatLogLevel
) : IsLoggableValidator {

    /**
     * Validates [priority] and [tag] of a message you would like logged.
     */
    override fun isLoggable(priority: Priority, tag: String): Boolean {
        return when (logLevel) {
            ChatLogLevel.NOTHING -> false
            ChatLogLevel.ALL -> true
            ChatLogLevel.DEBUG -> priority.level >= Priority.DEBUG.level
            ChatLogLevel.WARN -> priority.level >= Priority.WARN.level
            ChatLogLevel.ERROR -> priority.level >= Priority.ERROR.level
        }
    }
}