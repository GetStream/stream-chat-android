package io.getstream.chat.android.client.logger

public enum class ChatLogLevel(private val severity: Int) {
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
     * Show ERRORs only
     */
    ERROR(3),
    /**
     * Don't show any Logs.
     */
    NOTHING(4);

    internal fun isMoreOrEqualsThan(level: ChatLogLevel): Boolean {
        return level.severity >= severity
    }
}
