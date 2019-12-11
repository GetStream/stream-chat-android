package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;

public class StreamLogger {

    private static StreamLogger instance;
    private boolean showLogs;
    private Level loggingLevel = Level.INFO;

    private StreamLogger() {
    }

    public static StreamLogger getInstance() {
        if (instance == null) {
            instance = new StreamLogger();
        }
        return instance;
    }

    public void LogI(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.INFO)) {
            Log.d(getTag(classInstance), message);
        }
    }

    public void LogD(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.DEBUG)) {
            Log.d(getTag(classInstance), message);
        }
    }

    public void LogW(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.WARN)) {
            Log.w(getTag(classInstance), message);
        }
    }

    public void LogE(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.ERROR)) {
            Log.e(getTag(classInstance), message);
        }
    }

    private String getTag(@NonNull Class<?> classInstance) {
        return classInstance.getSimpleName();
    }

    public enum Level {
        /**
         * Show all Logs.
         */
        INFO(0),

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
        ERROR(3);

        private int severity;

        Level(int severity) {
            this.severity = severity;
        }

        public boolean isMoreOrEqualsThan(Level level) {
            return this.severity >= level.severity;
        }
    }

    public class Builder {

        /**
         * Enable logs for Build variant.
         * enable(BuildConfig.DEBUG) allow to showing logs only in Debug mode
         *
         * @param variant - Build Variant predicate
         * @return
         */
        public Builder enabled(boolean variant) {
            if (variant) {
                StreamLogger.this.showLogs = true;
            }

            return this;
        }

        public StreamLogger build() {
            return new StreamLogger();
        }
    }
}
