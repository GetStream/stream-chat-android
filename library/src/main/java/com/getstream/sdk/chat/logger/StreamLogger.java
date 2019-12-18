package com.getstream.sdk.chat.logger;

import androidx.annotation.NonNull;

public interface StreamLogger {

    void logT(@NonNull Throwable throwable);

    void logT(@NonNull Object classObj, @NonNull Throwable throwable);

    void logI(@NonNull Object classObj, @NonNull String message);

    void logD(@NonNull Object classObj, @NonNull String message);

    void logW(@NonNull Object classObj, @NonNull String message);

    void logE(@NonNull Object classObj, @NonNull String message);
}
