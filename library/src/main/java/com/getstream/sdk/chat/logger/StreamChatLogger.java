package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StreamChatLogger implements StreamLogger {

    private StreamLoggerLevel loggingLevel;
    private StreamLoggerHandler loggingHandler;

    private StreamChatLogger(@Nullable StreamLoggerLevel loggingLevel, @Nullable StreamLoggerHandler loggingHandler) {
        this.loggingLevel = loggingLevel == null ? StreamLoggerLevel.INFO : loggingLevel;
        this.loggingHandler = loggingHandler;
    }

    public void logT(@NonNull Throwable throwable) {
        throwable.printStackTrace();
        if (loggingHandler != null) {
            loggingHandler.logT(throwable);
        }
    }

    public void logT(@NonNull Object classObj, @NonNull Throwable throwable) {
        throwable.printStackTrace();
        if (loggingHandler != null) {
            loggingHandler.logT(getTag(classObj), throwable);
        }
    }

    public void logI(@NonNull Object classObj, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.INFO)) {
            Log.i(getTag(classObj), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logI(getTag(classObj), message);
        }
    }

    public void logD(@NonNull Object classObj, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.DEBUG)) {
            Log.d(getTag(classObj), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logD(getTag(classObj), message);
        }
    }

    public void logW(@NonNull Object classObj, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.WARN)) {
            Log.w(getTag(classObj), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logW(getTag(classObj), message);
        }
    }

    public void logE(@NonNull Object classObj, @NonNull String message) {
        Log.e(getTag(classObj), message);
        if (loggingHandler != null) {
            loggingHandler.logE(getTag(classObj), message);
        }
    }

    private String getTag(Object tag) {
        if (tag == null) return "null";
        if (tag instanceof String) {
            return (String) tag;
        } else {
            return tag.getClass().getSimpleName();
        }
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
            return new StreamChatLogger(loggingLevel, loggingHandler);
        }
    }
}
