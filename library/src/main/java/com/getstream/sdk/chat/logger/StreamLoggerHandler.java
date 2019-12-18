package com.getstream.sdk.chat.logger;

import androidx.annotation.NonNull;

public interface StreamLoggerHandler {

    void logT(@NonNull Throwable throwable);

    void logT(@NonNull String className, @NonNull Throwable throwable);

    void logI(@NonNull String className, @NonNull String message);

    void logD(@NonNull String className, @NonNull String message);

    void logW(@NonNull String className, @NonNull String message);

    void logE(@NonNull String className, @NonNull String message);
}
