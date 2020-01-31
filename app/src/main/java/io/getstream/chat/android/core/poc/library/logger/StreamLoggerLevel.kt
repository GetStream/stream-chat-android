package io.getstream.chat.android.core.poc.library.logger

enum class StreamLoggerLevel(private val severity: Int) {
    /**
     * Show all Logs.
     */
    ALL(0),
    /**
     * Show DEBUG, WARNING, ERROR logs
     */
    DEBUG(1),
    /**
     * Show WARNING and ERROR logs
     */
    WARN(2),
    /**
     * Show ERROR-s only
     */
    ERROR(3),
    /**
     * Don't show any Logs.
     */
    NOTHING(4);

    fun isMoreOrEqualsThan(level: StreamLoggerLevel): Boolean {
        return level.severity >= severity
    }

}