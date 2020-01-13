package io.getstream.chat.example;

import android.app.Application;
import android.widget.Toast;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.logger.StreamChatLogger;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.logger.StreamLoggerHandler;
import com.getstream.sdk.chat.logger.StreamLoggerLevel;
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination;
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.notifications.DeviceRegisteredListener;
import com.getstream.sdk.chat.notifications.NotificationMessageLoadListener;
import com.getstream.sdk.chat.notifications.NotificationsManager;
import com.getstream.sdk.chat.notifications.StreamNotificationsManager;
import com.getstream.sdk.chat.notifications.options.NotificationIntentProvider;
import com.getstream.sdk.chat.notifications.options.StreamNotificationOptions;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.style.StreamChatStyle;
import com.getstream.sdk.chat.utils.StringUtility;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import androidx.annotation.NonNull;
import io.fabric.sdk.android.Fabric;
import io.getstream.chat.example.utils.AppConfig;


public class BaseApplication extends Application {

    private StreamLogger logger;
    private ApiClientOptions apiClientOptions;
    private StreamChatStyle style;
    private AppConfig appConfig;
    private NotificationsManager notificationsManager;

    private static final String TAG = BaseApplication.class.getSimpleName();
    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());

        appConfig = new AppConfig(this);

        setupLogger();
        setupClientOptions();
        setupChatStyle();
        setupNotifications();
        initChat();

        Crashlytics.setString("apiKey", appConfig.getApiKey());
    }

    public AppConfig getAppConfig() {
        return appConfig;
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
                .loggingLevel(BuildConfig.DEBUG ? StreamLoggerLevel.ALL : StreamLoggerLevel.NOTHING)
                .setLoggingHandler(loggerHandler)
                .build();
    }

    private void setupClientOptions() {
        apiClientOptions = new ApiClientOptions.Builder()
                .BaseURL(appConfig.getApiEndPoint())
                .Timeout(appConfig.getApiTimeout())
                .CDNTimeout(appConfig.getCdnTimeout())
                .build();
    }

    private void setupChatStyle() {
        style = new StreamChatStyle.Builder()
                //.setDefaultFont(R.font.lilyofthe_valley)
                //.setDefaultFont("fonts/odibeesans_regular.ttf")
                .build();
    }

    private void setupNotifications() {
        // Configure and adding notification options for notifications
        StreamNotificationOptions notificationOptions = new StreamNotificationOptions();

        // Set custom intent provider for receiving message and events from firebase and WS
        notificationOptions.setNotificationIntentProvider(
                new NotificationIntentProvider() {
                    @Override
                    public PendingIntent getIntentForFirebaseMessage(@NonNull Context context, @NonNull RemoteMessage remoteMessage) {
                        Map<String, String> payload = remoteMessage.getData();
                        Intent intent = new Intent(context, ChannelActivity.class);
                        intent.putExtra(EXTRA_CHANNEL_TYPE, payload.get(StreamNotificationsManager.CHANNEL_TYPE_KEY));
                        intent.putExtra(EXTRA_CHANNEL_ID, payload.get(StreamNotificationsManager.CHANNEL_ID_KEY));
                        return PendingIntent.getActivity(context, 999,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT
                        );
                    }

                    @Override
                    public PendingIntent getIntentForWebSocketEvent(@NonNull Context context, @NonNull Event event) {
                        Intent intent = new Intent(context, ChannelActivity.class);
                        intent.putExtra(EXTRA_CHANNEL_TYPE, StringUtility.getChannelTypeFromCid(event.getCid()));
                        intent.putExtra(EXTRA_CHANNEL_ID, StringUtility.getChannelIdFromCid(event.getCid()));
                        return PendingIntent.getActivity(context, 999,
                                intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }
                }
        );

        // Device register listener
        DeviceRegisteredListener onDeviceRegistered = new DeviceRegisteredListener() {
            @Override
            public void onDeviceRegisteredSuccess() {
                // Device successfully registered on server
                StreamChat.getLogger().logI(TAG, "Device registered successfully");
            }

            @Override
            public void onDeviceRegisteredError(@NonNull String errorMessage, int errorCode) {
                // Some problem with registration
                StreamChat.getLogger().logE(TAG, "onDeviceRegisteredError: " + errorMessage + " Code: " + errorCode);
            }
        };

        NotificationMessageLoadListener messageListener = new NotificationMessageLoadListener() {
            @Override
            public void onLoadMessageSuccess(@NonNull Message message) {
                StreamChat.getLogger().logD(TAG, "On message loaded. Message:" + message);
            }

            @Override
            public void onLoadMessageFail(@NonNull String messageId) {
                StreamChat.getLogger().logD(TAG, "Message from notification load fails. MessageId:" + messageId);
            }
        };

        StreamNotificationsManager notificationsManager = new StreamNotificationsManager(notificationOptions, onDeviceRegistered);
        notificationsManager.setFailMessageListener(messageListener);

        this.notificationsManager = notificationsManager;
    }

    private void initChat() {
        StreamChat.Config configuration = new StreamChat.Config(this, appConfig.getApiKey());
        configuration.setApiClientOptions(apiClientOptions);
        configuration.setStyle(style);
        configuration.setLogger(logger);
        configuration.navigationHandler(destination -> {

            String url = "";

            if (destination instanceof WebLinkDestination) {
                url = ((WebLinkDestination) destination).url;
            } else if (destination instanceof AttachmentDestination) {
                url = ((AttachmentDestination) destination).url;
            }

            if (url.startsWith("https://your.domain.com")) {
                Toast.makeText(this, "Custom url handling: " + url, Toast.LENGTH_SHORT).show();
                // handle/change/update url
                // open your webview, system browser or your own activity
                // and return true to override default behaviour
                return true;
            } else {
                return false;
            }


        });
        configuration.setNotificationsManager(notificationsManager);
        StreamChat.init(configuration);

        Crashlytics.setString("apiKey", AppDataConfig.getCurrentApiKey());
    }
}
