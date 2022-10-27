package io.getstream.chat.docs.java.client.docusaurus;

import android.content.Context;

import androidx.annotation.NonNull;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.logger.ChatLoggerHandler;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/logging/">Logging</a>
 */
public class Logging {

    public void addLoggingHandler(Context context) {
        ChatClient client = new ChatClient.Builder("apiKey", context)
                .logLevel(ChatLogLevel.ALL)
                .loggerHandler(new ChatLoggerHandler() {
                    @Override
                    public void logV(@NonNull Object tag, @NonNull String message) {
                        // custom logging
                    }

                    @Override
                    public void logT(@NonNull Throwable throwable) {
                        // custom logging
                    }

                    @Override
                    public void logT(@NonNull Object tag, @NonNull Throwable throwable) {
                        // custom logging
                    }

                    @Override
                    public void logI(@NonNull Object tag, @NonNull String message) {
                        // custom logging
                    }

                    @Override
                    public void logD(@NonNull Object tag, @NonNull String message) {
                        // custom logging
                    }

                    @Override
                    public void logW(@NonNull Object tag, @NonNull String message) {
                        // custom logging
                    }

                    @Override
                    public void logE(@NonNull Object tag, @NonNull String message) {
                        // custom logging
                    }

                    @Override
                    public void logE(@NonNull Object tag, @NonNull String message, @NonNull Throwable throwable) {
                        // custom logging
                    }
                })
                .build();
    }
}
