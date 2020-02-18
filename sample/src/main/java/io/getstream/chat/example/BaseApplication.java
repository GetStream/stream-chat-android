package io.getstream.chat.example;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.navigation.ChatNavigationHandler;
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination;
import com.getstream.sdk.chat.navigation.destinations.ChatDestination;
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination;
import com.getstream.sdk.chat.style.StreamChatStyle;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.RemoteMessage;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

import io.fabric.sdk.android.Fabric;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig;
import io.getstream.chat.example.utils.AppConfig;


public class BaseApplication extends Application {

    private StreamLogger logger;
    private StreamChatStyle style;
    private AppConfig appConfig;

    private static final String TAG = BaseApplication.class.getSimpleName();
    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());

        appConfig = new AppConfig(this);

        setupChatStyle();
        initChat();

        Crashlytics.setString("apiKey", appConfig.getApiKey());
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    private void setupChatStyle() {
        style = new StreamChatStyle.Builder()
                //.setDefaultFont(R.font.lilyofthe_valley)
                //.setDefaultFont("fonts/odibeesans_regular.ttf")
                .build();
    }

    static String CHANNEL_TYPE_KEY = "channel-type-key";
    static String CHANNEL_ID_KEY = "channel-id-key";

    class NotificationConfig extends ChatNotificationConfig {

        @NotNull
        private final Context context;

        public NotificationConfig(@NotNull Context context) {
            super(context);
            this.context = context;
        }

        @NotNull
        @Override
        public PendingIntent getIntentForFirebaseMessage(@NotNull RemoteMessage remoteMessage) {
            Map<String, String> payload = remoteMessage.getData();
            Intent intent = new Intent(context, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, payload.get(CHANNEL_TYPE_KEY));
            intent.putExtra(EXTRA_CHANNEL_ID, payload.get(CHANNEL_ID_KEY));
            return PendingIntent.getActivity(context, getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        @NotNull
        @Override
        public PendingIntent getIntentForSocketEvent(@NotNull ChatEvent event) {
            Intent intent = new Intent(context, ChannelActivity.class);
            //intent.putExtra(EXTRA_CHANNEL_TYPE, StringUtility.getChannelTypeFromCid(event.getCid()));
            //intent.putExtra(EXTRA_CHANNEL_ID, StringUtility.getChannelIdFromCid(event.getCid()));
            return PendingIntent.getActivity(context, getRequestCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    private void initChat() {

        final Context context = this;

        StreamChat.Config config = new StreamChat.Config(appConfig.getApiKey(), appConfig.getCurrentUser().getToken(), context)
                .apiEndpoint(appConfig.getApiEndPoint())
                .apiTimout(appConfig.getApiTimeout())
                .cdnTimout(appConfig.getCdnTimeout())
                .style(style)
                .navigationHandler(new ChatNavigationHandler() {
                    @Override
                    public boolean navigate(ChatDestination destination) {
                        String url = "";

                        if (destination instanceof WebLinkDestination) {
                            url = ((WebLinkDestination) destination).url;
                        } else if (destination instanceof AttachmentDestination) {
                            url = ((AttachmentDestination) destination).url;
                        }

                        if (url.startsWith("https://your.domain.com")) {
                            Toast.makeText(context, "Custom url handling: " + url, Toast.LENGTH_SHORT).show();
                            // handle/change/update url
                            // open your webview, system browser or your own activity
                            // and return true to override default behaviour
                            return true;
                        } else {
                            return false;
                        }
                    }
                })
                .notifications(new NotificationConfig(context));

        StreamChat.init(config);
    }
}
