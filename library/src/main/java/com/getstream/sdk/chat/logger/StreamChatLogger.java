package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StreamChatLogger implements StreamLogger {

    private StreamLoggerLevel loggingLevel;
    private StreamLoggerHandler loggingHandler;

    private StreamChatLogger(@Nullable StreamLoggerLevel loggingLevel) {
        this.loggingLevel = loggingLevel == null ? StreamLoggerLevel.INFO : loggingLevel;
    }

    public void logI(@NonNull Class<?> classInstance, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.INFO)) {
            Log.i(getTag(classInstance), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logI(getTag(classInstance), message);
        }
    }

    public void logD(@NonNull Class<?> classInstance, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.DEBUG)) {
            Log.d(getTag(classInstance), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logD(getTag(classInstance), message);
        }
    }

    public void logW(@NonNull Class<?> classInstance, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.WARN)) {
            Log.w(getTag(classInstance), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logW(getTag(classInstance), message);
        }
    }

    public void logE(@NonNull Class<?> classInstance, @NonNull String message) {
        Log.e(getTag(classInstance), message);
        if (loggingHandler != null) {
            loggingHandler.logE(getTag(classInstance), message);
        }
    }

    private String getTag(@NonNull Class<?> classInstance) {
        return classInstance.getSimpleName();
    }

    public static class Builder {

        private StreamLoggerLevel loggingLevel;
        private StreamLoggerHandler loggingHandler;

        /**
         * Set logging level
         *
         * @param level - Logging {@link StreamLoggerLevel}
         * @return - builder
         */
        public Builder loggingLevel(StreamLoggerLevel level) {
            this.loggingLevel = level;

            return this;
        }

        public Builder setLoggingHandler(StreamLoggerHandler handler) {
            this.loggingHandler = handler;

            return this;
        }

        public StreamChatLogger build() {
            return new StreamChatLogger(loggingLevel);
        }
    }
}
