package com.getstream.sdk.chat;

import android.content.Context;

import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.navigation.ChatNavigationHandler;
import com.getstream.sdk.chat.navigation.StreamChatNavigator;
import com.getstream.sdk.chat.navigation.StreamChatNavigatorImpl;
import com.getstream.sdk.chat.storage.InMemoryCache;
import com.getstream.sdk.chat.style.FontsManager;
import com.getstream.sdk.chat.style.FontsManagerImpl;
import com.getstream.sdk.chat.style.StreamChatStyle;
import com.getstream.sdk.chat.utils.strings.StringsProvider;
import com.getstream.sdk.chat.utils.strings.StringsProviderImpl;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.ConnectedEvent;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig;
import io.getstream.chat.android.client.socket.SocketListener;

public class StreamChat {

    private static MutableLiveData<OnlineStatus> onlineStatus;
    private static MutableLiveData<Number> totalUnreadMessages;
    private static MutableLiveData<Number> unreadChannels;

    private static MutableLiveData<User> currentUser;
    private static StringsProvider stringsProvider;
    private static StreamChatStyle chatStyle = new StreamChatStyle.Builder().build();
    private static FontsManager fontsManager;
    private static StreamChatNavigator navigator = new StreamChatNavigatorImpl();
    private static ClientInterceptor client;

    public static StreamChatNavigator getNavigator() {
        return navigator;
    }

    public static LiveData<OnlineStatus> getOnlineStatus() {
        return onlineStatus;
    }

    /**
     * @return LiveData object for the total unread messages
     */
    public static LiveData<Number> getTotalUnreadMessages() {
        return totalUnreadMessages;
    }

    /**
     * @return LiveData object for the number of unread channels
     */
    public static LiveData<Number> getUnreadChannels() {
        return unreadChannels;
    }

    /**
     * @return LiveData object for the current user
     */
    public static LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public static ChatClient getInstance() {
        return ChatClient.Companion.instance();
    }

    public static StringsProvider getStrings() {
        return stringsProvider;
    }

    public static InMemoryCache cache() {
        return client;
    }

    public static String signFileUrl(String url) {
        //TODO: llc add sign url
        return url;
    }

    public static String signGlideUrl(String url) {
        //TODO: llc add sign url
        return url;
    }

    /**
     * For unit tests purposes only
     */
    public static void setStringsProvider(StringsProvider stringsProvider) {
        StreamChat.stringsProvider = stringsProvider;
    }

    public static void init(Config config) {

        chatStyle = config.style;
        stringsProvider = new StringsProviderImpl(config.appContext);
        fontsManager = new FontsManagerImpl(config.appContext);

        onlineStatus = new MutableLiveData<>(OnlineStatus.NOT_INITIALIZED);
        currentUser = new MutableLiveData<>();
        totalUnreadMessages = new MutableLiveData<>();
        unreadChannels = new MutableLiveData<>();

        ChatClient c = new ChatClient.Builder(config.apiKey, config.appContext)
                .logLevel(config.logLevel)
                .notifications(config.notificationConfig)
                .build();
        client = new ClientInterceptor(c);

        setupLifecycleObserver();

        client.addSocketListener(new SocketListener() {
            @Override
            public void onConnected(@NotNull ConnectedEvent event) {
                onlineStatus.postValue(OnlineStatus.CONNECTED);
                currentUser.postValue(event.me);
            }

            @Override
            public void onConnecting() {
                onlineStatus.postValue(OnlineStatus.CONNECTING);
            }

            @Override
            public void onError(@NotNull ChatError error) {
                onlineStatus.postValue(OnlineStatus.FAILED);
            }

            @Override
            public void onEvent(@NotNull ChatEvent event) {

                Integer totalUnreadCount = event.getTotalUnreadCount();
                Integer unreadChannels = event.getUnreadChannels();

                if (totalUnreadCount != null) {
                    StreamChat.totalUnreadMessages.postValue(totalUnreadCount);
                }

                if (unreadChannels != null) {
                    StreamChat.unreadChannels.postValue(unreadChannels);
                }
            }
        });
    }

    private static void setupLifecycleObserver() {
        new StreamLifecycleObserver(new LifecycleHandler() {
            @Override
            public void resume() {
                client.reconnectSocket();
            }

            @Override
            public void stopped() {
                client.disconnectSocket();
            }
        });
    }

    public static void initStyle(StreamChatStyle style) {
        chatStyle = style;
    }

    @NotNull
    public static StreamChatStyle getChatStyle() {
        return chatStyle;
    }

    @NotNull
    public static FontsManager getFontsManager() {
        return fontsManager;
    }

    public static ChatLogger getLogger() {
        return ChatLogger.Companion.getInstance();
    }


    public static class Config {

        private final String apiKey;
        private final Context appContext;
        private ChatLogLevel logLevel = ChatLogLevel.NOTHING;
        private ChatNotificationConfig notificationConfig;
        private String cdnEndpoint;
        private String apiEndpoint;
        private long cdnTimeout;
        private long apiTimeout;
        private ChatNavigationHandler navigationHandler;
        private StreamChatStyle style;

        public Config(String apiKey, Context appContext) {

            this.apiKey = apiKey;
            this.appContext = appContext;
        }

        public Config apiEndpoint(String apiEndpoint) {
            this.apiEndpoint = apiEndpoint;
            return this;
        }

        public Config cdnEndpoint(String cdnEndpoint) {
            this.cdnEndpoint = cdnEndpoint;
            return this;
        }

        public Config cdnTimout(long cdnTimeout) {
            this.cdnTimeout = cdnTimeout;
            return this;
        }

        public Config apiTimout(long apiTimeout) {
            this.apiTimeout = apiTimeout;
            return this;
        }

        public Config logLevel(ChatLogLevel logLevel) {
            this.logLevel = logLevel;
            return this;
        }

        public Config notifications(ChatNotificationConfig notificationConfig) {
            this.notificationConfig = notificationConfig;
            return this;
        }

        public Config navigationHandler(ChatNavigationHandler navigationHandler) {
            this.navigationHandler = navigationHandler;
            return this;
        }

        public Config style(@NonNull StreamChatStyle style) {
            this.style = style;
            return this;
        }
    }
}
