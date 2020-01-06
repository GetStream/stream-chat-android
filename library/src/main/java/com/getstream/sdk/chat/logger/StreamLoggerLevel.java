package com.getstream.sdk.chat.logger;

public enum StreamLoggerLevel {

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

    private int severity;

    StreamLoggerLevel(int severity) {
        this.severity = severity;
    }

    public boolean isMoreOrEqualsThan(StreamLoggerLevel level) {
        return level.severity >= this.severity;
    }
}