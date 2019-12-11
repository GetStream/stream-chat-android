package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StreamChatLogger implements StreamLogger {

    private boolean showLogs;
    private Level loggingLevel;

    private StreamChatLogger(boolean showLogs, @Nullable Level loggingLevel) {
        this.showLogs = showLogs;
        this.loggingLevel = loggingLevel == null ? Level.INFO : loggingLevel;
    }

    public void logI(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.INFO)) {
            Log.i(getTag(classInstance), message);
        }
    }

    public void logD(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.DEBUG)) {
            Log.d(getTag(classInstance), message);
        }
    }

    public void logW(@NonNull Class<?> classInstance, @NonNull String message) {
        if (showLogs && loggingLevel.isMoreOrEqualsThan(Level.WARN)) {
            Log.w(getTag(classInstance), message);
        }
    }

    public void logE(@NonNull Class<?> classInstance, @NonNull String message) {
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
            return level.severity >= this.severity;
        }
    }

    public static class Builder {

        private boolean showLogs;
        private Level loggingLevel;

        /**
         * Enable logs for Build variant.
         * enable(BuildConfig.DEBUG) allow to showing logs only in Debug mode
         *
         * @param variant - Build Variant predicate
         * @return - builder
         */
        public Builder enabled(boolean variant) {
            if (variant) {
                this.showLogs = true;
            }

            return this;
        }

        /**
         * Set logging level
         * @param level - Logging {@link Level}
         * @return
         */
        public Builder loggingLevel(Level level) {
            this.loggingLevel = level;

            return this;
        }

        public StreamChatLogger build() {
            return new StreamChatLogger(showLogs, loggingLevel);
        }
    }
}
