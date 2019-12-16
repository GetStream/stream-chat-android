package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;

public class StreamChatSilentLogger implements StreamLogger {

    @Override
    public void logI(@NonNull Class<?> classInstance, @NonNull String message) {
        // unused
    }

    @Override
    public void logD(@NonNull Class<?> classInstance, @NonNull String message) {
        // unused
    }

    @Override
    public void logW(@NonNull Class<?> classInstance, @NonNull String message) {
        // unused
    }

    @Override
    public void logE(@NonNull Class<?> classInstance, @NonNull String message) {
        Log.e(classInstance.getSimpleName(), message);
    }
}
