package com.getstream.sdk.chat;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.logger.StreamChatSilentLogger;
import com.getstream.sdk.chat.logger.StreamLogger;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.core.ClientState;
import com.getstream.sdk.chat.style.FontsManager;
import com.getstream.sdk.chat.style.FontsManagerImpl;
import com.getstream.sdk.chat.style.StreamChatStyle;
import com.getstream.sdk.chat.utils.strings.StringsProvider;
import com.getstream.sdk.chat.utils.strings.StringsProviderImpl;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StreamChat {
    private static final String TAG = StreamChat.class.getSimpleName();

    private static Client INSTANCE;

    private static MutableLiveData<OnlineStatus> onlineStatus;
    private static MutableLiveData<Number> totalUnreadMessages;
    private static MutableLiveData<Number> unreadChannels;

    private static MutableLiveData<User> currentUser;
    private static Integer lastTotalUnreadMessages;
    private static Integer lastUnreadChannels;
    private static boolean lifecycleStopped;
    private static boolean userWasInitialized;
    private static Context context;
    private static StringsProvider stringsProvider;
    private static StreamChatStyle chatStyle = new StreamChatStyle.Builder().build();
    private static FontsManager fontsManager;
    private static StreamLogger logger;

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

    public static synchronized Client getInstance(final Context context) {
        if (INSTANCE == null) {
            throw new RuntimeException("You must initialize the API client first, make sure to call StreamChat.initialize");
        } else {
            return INSTANCE;
        }
    }

    public static Context getContext() {
        return context;
    }

    public static StringsProvider getStrings() {
        return stringsProvider;
    }

    /**
     * For unit tests purposes only
     */
    public static void setStringsProvider(StringsProvider stringsProvider) {
        StreamChat.stringsProvider = stringsProvider;
    }

    public static void setLogger(StreamLogger logger) {
        StreamChat.logger = logger;
    }

    private static void initComponents(String apiKey, ApiClientOptions apiClientOptions) {
        stringsProvider = new StringsProviderImpl(context);
        fontsManager = new FontsManagerImpl(context);
        INSTANCE = new Client(apiKey, apiClientOptions, new ConnectionLiveData(StreamChat.context));

        onlineStatus = new MutableLiveData<>(OnlineStatus.NOT_INITIALIZED);
        currentUser = new MutableLiveData<>();
        totalUnreadMessages = new MutableLiveData<>();
        unreadChannels = new MutableLiveData<>();

        INSTANCE.setContext(StreamChat.context);
    }

    private static void handleConnectedUser() {
        INSTANCE.onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                userWasInitialized = true;
                onlineStatus.postValue(OnlineStatus.CONNECTED);
            }

            @Override
            public void onError(String errMsg, int errCode) {
                StreamChat.logD(this.getClass(), "handleConnectedUser: error: " + errMsg + ":" + errCode);
            }
        });
    }

    private static void observeSetUserCompleted() {
        INSTANCE.onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                Log.i(TAG, "set user worked out well");
                setupEventListeners();
                currentUser.postValue(user);

                setupLifecycleObserver();
            }

            @Override
            public void onError(String errMsg, int errCode) {
                onlineStatus.postValue(OnlineStatus.FAILED);
            }
        });
    }

    private static synchronized void setupEventListeners() {
        StreamChat.logI(StreamChat.class, "setupEventListeners");

        INSTANCE.addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionChanged(Event event) {
                StreamChat.logW(this.getClass(), "connection status changed to " + (event.getOnline() ? "online" : "offline"));

                if (event.getOnline()) {
                    onlineStatus.postValue(OnlineStatus.CONNECTING);
                } else {
                    onlineStatus.postValue(OnlineStatus.FAILED);
                }
                handleConnectedUser();
            }

            @Override
            public void onConnectionRecovered(Event event) {
                StreamChat.logW(this.getClass(), "connection recovered!");
                List<Channel> channels = INSTANCE.getActiveChannels();
                if (channels == null) {
                    StreamChat.logW(this.getClass(), "nothing to recover");
                } else {
                    StreamChat.logW(this.getClass(), channels.size() + " channels to recover!");
                }
                onlineStatus.postValue(OnlineStatus.CONNECTED);
            }

            @Override
            public void onAnyEvent(Event event) {
                ClientState state = INSTANCE.getState();
                if (state.getTotalUnreadCount() != null) {
                    // post the value if it changed since last time
                    if (!state.getTotalUnreadCount().equals(lastTotalUnreadMessages)) {
                        lastTotalUnreadMessages = state.getTotalUnreadCount();
                        totalUnreadMessages.postValue(lastTotalUnreadMessages);
                    }
                    if (!state.getUnreadChannels().equals(lastUnreadChannels)) {
                        lastUnreadChannels = state.getUnreadChannels();
                        unreadChannels.postValue(lastUnreadChannels);
                    }
                    if (event.getMe() != null) {
                        currentUser.postValue(state.getCurrentUser());
                    }
                }
            }
        });
    }

    private static void setupLifecycleObserver() {
        new StreamLifecycleObserver(new LifecycleHandler() {
            @Override
            public void resume() {
                Log.i(TAG, "detected resume");
                if (lifecycleStopped && userWasInitialized) {
                    lifecycleStopped = false;
                    INSTANCE.reconnectWebSocket();
                }
            }

            @Override
            public void stopped() {
                Log.i(TAG, "detected stop");
                lifecycleStopped = true;
                if (INSTANCE != null) {
                    INSTANCE.disconnectWebSocket();
                }
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

    public static void logI(@NonNull Class<?> classInstance, @NonNull String message) {
        logger.logI(classInstance, message);
    }

    public static void logD(@NonNull Class<?> classInstance, @NonNull String message) {
        logger.logD(classInstance, message);
    }

    public static void logW(@NonNull Class<?> classInstance, @NonNull String message) {
        logger.logW(classInstance, message);
    }

    public static void logE(@NonNull Class<?> classInstance, @NonNull String message) {
        logger.logE(classInstance, message);
    }

    public static synchronized boolean init(@NonNull Config config) {
        ApiClientOptions options = config.getApiClientOptions() != null ? config.getApiClientOptions() : new ApiClientOptions();
        return init(config.getApiKey(), options, context);
    }

    public static synchronized boolean init(String apiKey, Context context) {
        return init(apiKey, new ApiClientOptions(), context, new StreamChatSilentLogger());
    }

    public static synchronized boolean init(String apiKey, ApiClientOptions apiClientOptions, @NonNull Context context, StreamLogger logger) {
        if (INSTANCE != null) {
            return true;
        }
        StreamChat.logger = logger;
        StreamChat.logI(StreamChat.class, "calling init");
        synchronized (Client.class) {
            if (INSTANCE == null) {
                StreamChat.context = context;

                initComponents(apiKey, apiClientOptions);

                handleConnectedUser();
                observeSetUserCompleted();
            }
            return true;
        }
    }

    public static class Config {

        private String apiKey;
        private ApiClientOptions apiClientOptions;

        public Config(@NonNull Context context, @NonNull String apiKey) {
            StreamChat.context = context;
            this.apiKey = apiKey;
        }

        /**
         * Set custom api client options.
         *
         * @param apiClientOptions - {@link ApiClientOptions}
         */
        public void setApiClientOptions(@NonNull ApiClientOptions apiClientOptions) {
            this.apiClientOptions = apiClientOptions;
        }

        /**
         * Set custom chat style.
         *
         * @param style - {@link StreamChatStyle}
         */
        public void setStyle(@NonNull StreamChatStyle style) {
            StreamChat.chatStyle = style;
        }

        private String getApiKey() {
            return apiKey;
        }

        private ApiClientOptions getApiClientOptions() {
            return apiClientOptions;
        }
    }
}
