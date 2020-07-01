package com.getstream.sdk.chat;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.navigation.ChatNavigationHandler;
import com.getstream.sdk.chat.navigation.ChatNavigator;
import com.getstream.sdk.chat.navigation.ChatNavigatorImpl;
import com.getstream.sdk.chat.style.ChatFonts;
import com.getstream.sdk.chat.utils.strings.ChatStrings;

import org.jetbrains.annotations.NotNull;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.events.ChatEvent;
import io.getstream.chat.android.client.events.ConnectedEvent;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.socket.InitConnectionListener;
import io.getstream.chat.android.client.socket.SocketListener;
import io.getstream.chat.android.livedata.ChatDomain;
import kotlin.UninitializedPropertyAccessException;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

class ChatImpl implements Chat {
    private MutableLiveData<OnlineStatus> onlineStatus = new MutableLiveData<>(OnlineStatus.NOT_INITIALIZED);
    private MutableLiveData<Number> unreadMessages = new MutableLiveData<>();
    private MutableLiveData<Number> unreadChannels = new MutableLiveData<>();
    private MutableLiveData<User> currentUser = new MutableLiveData<>();

    private final ChatNavigator navigator = new ChatNavigatorImpl();
    private final ChatStrings chatStrings;
    private final ChatFonts chatFonts;
    private final UrlSigner urlSigner;
    private final ChatMarkdown markdown;
    private final String apiKey;
    private final Context context;
    private final boolean offlineEnabled;

    ChatImpl(ChatFonts chatFonts,
             ChatStrings chatStrings,
             ChatNavigationHandler navigationHandler,
             UrlSigner urlSigner,
             ChatMarkdown markdown,
             String apiKey,
             Context context,
             boolean offlineEnabled) {
        this.chatStrings = chatStrings;
        this.chatFonts = chatFonts;
        this.urlSigner = urlSigner;
        this.markdown = markdown;
        this.apiKey = apiKey;
        this.context = context;
        this.offlineEnabled = offlineEnabled;

        navigator.setHandler(navigationHandler);
        new ChatClient.Builder(this.apiKey, context).build();
        ChatLogger.Companion.getInstance().logI("Chat", "Initialized: " + getVersion());
    }

    @Override
    public ChatNavigator getNavigator() {
        return navigator;
    }

    @Override
    public LiveData<OnlineStatus> getOnlineStatus() {
        return onlineStatus;
    }

    @Override
    public LiveData<Number> getUnreadMessages() {
        return unreadMessages;
    }

    @Override
    public LiveData<Number> getUnreadChannels() {
        return unreadChannels;
    }

    @Override
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    @Override
    public ChatStrings getStrings() {
        return chatStrings;
    }

    @Override
    public UrlSigner urlSigner() {
        return urlSigner;
    }

    @Override
    @NotNull
    public ChatFonts getFonts() {
        return chatFonts;
    }

    @Override
    @NotNull
    public ChatMarkdown getMarkdown() {
        return markdown;
    }

    @Override
    public String getVersion() {
        return BuildConfig.BUILD_TYPE + ":" + BuildConfig.VERSION_NAME;
    }

    @Override
    public void setUser(@NotNull User user,
                        @NotNull String userToken,
                        @NotNull InitConnectionListener callbacks) {
        disconnectChatDomainIfAlreadyInitialized();
        final ChatClient client = ChatClient.instance();
        final ChatDomain.Builder domainBuilder = new ChatDomain.Builder(context, client, user);
        if (offlineEnabled) {
            domainBuilder.offlineEnabled();
        }
        final ChatDomain chatDomain = domainBuilder.userPresenceEnabled().build();
        client.setUser(user, userToken, new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                chatDomain.setCurrentUser(user);
                callbacks.onSuccess(data);
            }

            @Override
            public void onError(@NotNull ChatError error) {
                callbacks.onError(error);
            }
        });

        init();
    }

    private void disconnectChatDomainIfAlreadyInitialized() {
        try {
            final ChatDomain chatDomain = ChatDomain.instance();
            BuildersKt.launch(GlobalScope.INSTANCE,
                    Dispatchers.getIO(),
                    CoroutineStart.DEFAULT,
                    (scope, continuation) -> chatDomain.disconnect(continuation));
        } catch(UninitializedPropertyAccessException e) {
            ChatLogger.Companion.get("ChatImpl").logD("ChatDomain was not initialized yet. No need to disconnect.");
        }
    }

    protected void init() {
        initSocketListener();
        initLifecycle();
    }

    private void initLifecycle() {
        new StreamLifecycleObserver(new LifecycleHandler() {
            @Override
            public void resume() {
                client().reconnectSocket();
            }

            @Override
            public void stopped() {
                client().disconnectSocket();
            }
        });
    }

    private void initSocketListener() {
        client().addSocketListener(new SocketListener() {
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
                    ChatImpl.this.unreadMessages.postValue(totalUnreadCount);
                }

                if (unreadChannels != null) {
                    ChatImpl.this.unreadChannels.postValue(unreadChannels);
                }
            }
        });
    }

    private ChatClient client() {
        return ChatClient.instance();
    }
}
