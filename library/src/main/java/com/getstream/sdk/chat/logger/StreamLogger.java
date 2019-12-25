package com.getstream.sdk.chat.logger;

import androidx.annotation.NonNull;

public interface StreamLogger {

    void logT(@NonNull Throwable throwable);

    void logT(@NonNull Object tag, @NonNull Throwable throwable);

    void logI(@NonNull Object tag, @NonNull String message);

    void logD(@NonNull Object tag, @NonNull String message);

    void logW(@NonNull Object tag, @NonNull String message);

    void logE(@NonNull Object tag, @NonNull String message);
}
