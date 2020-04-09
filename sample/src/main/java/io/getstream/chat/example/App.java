package io.getstream.chat.example;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.getstream.sdk.chat.Chat;
import com.getstream.sdk.chat.navigation.destinations.AttachmentDestination;
import com.getstream.sdk.chat.navigation.destinations.WebLinkDestination;
import com.getstream.sdk.chat.style.ChatStyle;
import com.google.firebase.FirebaseApp;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.fabric.sdk.android.Fabric;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.models.Channel;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.notifications.NotificationLoadDataListener;
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig;
import io.getstream.chat.android.livedata.ChatRepo;
import io.getstream.chat.example.utils.AppConfig;


public class App extends Application {

    private ChatStyle style;
    private AppConfig appConfig;

    private static final String TAG = App.class.getSimpleName();
    public static final String EXTRA_CHANNEL_TYPE = "io.getstream.chat.example.CHANNEL_TYPE";
    public static final String EXTRA_CHANNEL_ID = "io.getstream.chat.example.CHANNEL_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        FirebaseApp.initializeApp(getApplicationContext());

        appConfig = new AppConfig(this);

        setupChatStyle();
        //initDefaultChat();
        initCustomChat();



        Crashlytics.setString("apiKey", appConfig.getApiKey());
    }

    public AppConfig getAppConfig() {
        return appConfig;
    }

    private void setupChatStyle() {
        style = new ChatStyle.Builder()
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
        public Intent getNewMessageIntent(@NotNull String messageId, @NotNull String channelType, @NotNull String channelId) {
            Intent intent = new Intent(context, ChannelActivity.class);
            intent.putExtra(EXTRA_CHANNEL_TYPE, channelType);
            intent.putExtra(EXTRA_CHANNEL_ID, channelId);
            return intent;
        }

        @Nullable
        @Override
        public NotificationLoadDataListener getDataLoadListener() {
            return new NotificationLoadDataListener() {
                @Override
                public void onLoadSuccess(@NotNull Channel channel, @NotNull Message message) {
                    if (channel == null) {

                    }
                }

                @Override
                public void onLoadFail(@NotNull String s, @NotNull ChatError chatError) {
                    if (s == null) {

                    }
                }
            };
        }
    }

    private void initDefaultChat() {

        String apiKey = appConfig.getApiKey();

        Chat chat = new Chat.Builder(apiKey, this).build();
    }

    private void initCustomChat() {


        String apiKey = appConfig.getApiKey();
        Context context = this;

        Chat chat = new Chat.Builder(apiKey, this)
                .apiTimeout(appConfig.getApiTimeout())
                .cdnTimeout(appConfig.getCdnTimeout())
                .logLevel(ChatLogLevel.ALL)
                .style(style)
                .navigationHandler(destination -> {
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
                })
                .notifications(new NotificationConfig(context))
                .build();


    }
}
