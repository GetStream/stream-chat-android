package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StreamChatLogger implements StreamLogger {

    private StreamLoggerLevel loggingLevel;
    private StreamLoggerHandler loggingHandler;

    private StreamChatLogger(@Nullable StreamLoggerLevel loggingLevel, @Nullable StreamLoggerHandler loggingHandler) {
        this.loggingLevel = loggingLevel == null ? StreamLoggerLevel.NOTHING : loggingLevel;
        this.loggingHandler = loggingHandler;
    }

    public void logI(@NonNull Object tag, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.ALL)) {
            Log.i(getTag(tag), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logI(getTag(tag), message);
        }
    }

    public void logD(@NonNull Object tag, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.DEBUG)) {
            Log.d(getTag(tag), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logD(getTag(tag), message);
        }
    }

    public void logW(@NonNull Object tag, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.WARN)) {
            Log.w(getTag(tag), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logW(getTag(tag), message);
        }
    }

    public void logE(@NonNull Object tag, @NonNull String message) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.ERROR)) {
            Log.e(getTag(tag), message);
        }
        if (loggingHandler != null) {
            loggingHandler.logE(getTag(tag), message);
        }
    }

    public void logT(@NonNull Throwable throwable) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.ERROR)) {
            throwable.printStackTrace();
        }
        if (loggingHandler != null) {
            loggingHandler.logT(throwable);
        }
    }

    public void logT(@NonNull Object tag, @NonNull Throwable throwable) {
        if (loggingLevel.isMoreOrEqualsThan(StreamLoggerLevel.ERROR)) {
            throwable.printStackTrace();
        }
        if (loggingHandler != null) {
            loggingHandler.logT(getTag(tag), throwable);
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
