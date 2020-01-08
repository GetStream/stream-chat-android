package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;

public class StreamChatSilentLogger implements StreamLogger {

    @Override
    public void logI(@NonNull Object tag, @NonNull String message) {
        // unused
    }

    @Override
    public void logD(@NonNull Object tag, @NonNull String message) {
        // unused
    }

    @Override
    public void logW(@NonNull Object tag, @NonNull String message) {
        // unused
    }

    @Override
    public void logE(@NonNull Object tag, @NonNull String message) {
        // unused
    }

    @Override
    public void logT(@NonNull Throwable throwable) {
        // unused
    }

    @Override
    public void logT(@NonNull Object tag, @NonNull Throwable throwable) {
        // unused
    }
}
