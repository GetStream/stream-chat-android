package com.getstream.sdk.chat.logger;

import android.util.Log;

import androidx.annotation.NonNull;

public class StreamChatSilentLogger implements StreamLogger {

    @Override
    public void logT(@NonNull Throwable throwable) {
        throwable.printStackTrace();
    }

    @Override
    public void logT(@NonNull Object classObj, @NonNull Throwable throwable) {
        // unused
    }

    @Override
    public void logI(@NonNull Object classObj, @NonNull String message) {
        // unused
    }

    @Override
    public void logD(@NonNull Object classObj, @NonNull String message) {
        // unused
    }

    @Override
    public void logW(@NonNull Object classObj, @NonNull String message) {
        // unused
    }

    @Override
    public void logE(@NonNull Object classObj, @NonNull String message) {
        Log.e(classObj.getClass().getSimpleName(), message);
    }
}
