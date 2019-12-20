package io.getstream.chat.example;

import android.app.Application;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.logger.StreamChatLogger;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.logger.StreamLoggerHandler;
import com.getstream.sdk.chat.logger.StreamLoggerLevel;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.style.StreamChatStyle;
import com.google.firebase.FirebaseApp;

import io.fabric.sdk.android.Fabric;
import io.getstream.chat.example.utils.AppDataConfig;


public class BaseApplication extends Application {
    private StreamLogger logger;
    private ApiClientOptions apiClientOptions;
    private StreamChatStyle style;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());

        AppDataConfig.init(this);

        setupLogger();
        setupClientOptions();
        setupChatStyle();
        initChat();

        Crashlytics.setString("apiKey", AppDataConfig.getCurrentApiKey());
    }

    private void setupLogger() {
        StreamLoggerHandler loggerHandler = new StreamLoggerHandler() {
            @Override
            public void logT(@NonNull Throwable throwable) {
                // display throwable logs here
            }

            @Override
            public void logT(@NonNull String className, @NonNull Throwable throwable) {
                // display throwable logs here
            }

            @Override
            public void logI(@NonNull String className, @NonNull String message) {
                // display info logs here
            }

            @Override
            public void logD(@NonNull String className, @NonNull String message) {
                // display debug logs here
            }

            @Override
            public void logW(@NonNull String className, @NonNull String message) {
                // display warning logs here
            }

            @Override
            public void logE(@NonNull String className, @NonNull String message) {
                // display error logs here
            }
        };

        logger = new StreamChatLogger.Builder()
                .loggingLevel(StreamLoggerLevel.INFO)
                .setLoggingHandler(loggerHandler)
                .build();
    }

    private void setupClientOptions() {
        apiClientOptions = new ApiClientOptions.Builder()
                .BaseURL(AppDataConfig.getApiEndpoint())
                .Timeout(AppDataConfig.getApiTimeout())
                .CDNTimeout(AppDataConfig.getCdnTimeout())
                .build();
    }

    private void setupChatStyle() {
        style = new StreamChatStyle.Builder()
                //.setDefaultFont(R.font.lilyofthe_valley)
                //.setDefaultFont("fonts/odibeesans_regular.ttf")
                .build();
    }

    private void initChat() {
        StreamChat.Config configuration = new StreamChat.Config(this, AppDataConfig.getCurrentApiKey());
        configuration.setApiClientOptions(apiClientOptions);
        configuration.setStyle(style);
        configuration.setLogger(logger);
        StreamChat.init(configuration);
    }
}
