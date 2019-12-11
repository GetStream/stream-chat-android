package com.getstream.sdk.chat.logger;

import androidx.annotation.NonNull;

public interface StreamLogger {

    void logI(@NonNull Class<?> classInstance, @NonNull String message);

    void logD(@NonNull Class<?> classInstance, @NonNull String message);

    void logW(@NonNull Class<?> classInstance, @NonNull String message);

    void logE(@NonNull Class<?> classInstance, @NonNull String message);
}
