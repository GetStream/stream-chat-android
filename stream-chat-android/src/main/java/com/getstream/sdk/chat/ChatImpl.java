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
import org.jetbrains.annotations.Nullable;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.errors.ChatError;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.logger.ChatLogger;
import io.getstream.chat.android.client.logger.ChatLoggerHandler;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler;
import io.getstream.chat.android.client.socket.InitConnectionListener;
import io.getstream.chat.android.client.uploader.FileUploader;
import io.getstream.chat.android.livedata.ChatDomain;
import kotlin.Unit;
import kotlinx.coroutines.BuildersKt;
import kotlinx.coroutines.CoroutineStart;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.GlobalScope;

class ChatImpl implements Chat {
    private final ChatNavigationHandler navigationHandler;
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
    private final ChatNotificationHandler chatNotificationHandler;

    ChatImpl(ChatFonts chatFonts,
             ChatStrings chatStrings,
             @Nullable ChatNavigationHandler navigationHandler,
             UrlSigner urlSigner,
             ChatMarkdown markdown,
             String apiKey,
             Context context,
             boolean offlineEnabled,
             ChatNotificationHandler chatNotificationHandler,
             ChatLogLevel chatLogLevel,
             @Nullable ChatLoggerHandler chatLoggerHandler,
             @Nullable FileUploader fileUploader) {
        this.chatStrings = chatStrings;
        this.chatFonts = chatFonts;
        this.navigationHandler = navigationHandler;
        this.urlSigner = urlSigner;
        this.markdown = markdown;
        this.apiKey = apiKey;
        this.context = context;
        this.offlineEnabled = offlineEnabled;
        this.chatNotificationHandler = chatNotificationHandler;

        if (navigationHandler != null) {
            navigator.setHandler(navigationHandler);
        }

        ChatClient.Builder chatBuilder = new ChatClient.Builder(this.apiKey, context)
                .notifications(chatNotificationHandler)
                .logLevel(chatLogLevel);

        if (chatLoggerHandler != null) {
            chatBuilder.loggerHandler(chatLoggerHandler);
        }

        if (fileUploader != null) {
            chatBuilder.fileUploader(fileUploader);
        }

        chatBuilder.build();



        ChatLogger.Companion.getInstance().logI("Chat", "Initialized: " + getVersion());
    }

    @NotNull
    @Override
    public ChatNavigator getNavigator() {
        return navigator;
    }

    @NotNull
    @Override
    public LiveData<OnlineStatus> getOnlineStatus() {
        return onlineStatus;
    }

    @NotNull
    @Override
    public LiveData<Number> getUnreadMessages() {
        return unreadMessages;
    }

    @NotNull
    @Override
    public LiveData<Number> getUnreadChannels() {
        return unreadChannels;
    }

    @NotNull
    @Override
    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    @NotNull
    @Override
    public ChatStrings getStrings() {
        return chatStrings;
    }

    @NotNull
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

    @NotNull
    @Override
    public String getVersion() {
        return BuildConfig.STREAM_CHAT_UI_VERSION + "-" + BuildConfig.BUILD_TYPE;
    }

    @Override
    public void setUser(@NotNull User user,
                        @NotNull String userToken,
                        @NotNull InitConnectionListener callbacks) {
        final ChatClient client = ChatClient.instance();
        // this behaviour is wrong, note how ChatClient raises an error if you try to call setUser
        // while already being connected
        client.disconnect();
        disconnectChatDomainIfAlreadyInitialized();
        final ChatDomain.Builder domainBuilder = new ChatDomain.Builder(context, client, user);
        if (offlineEnabled) {
            domainBuilder.offlineEnabled();
        }
        ChatDomain domain = domainBuilder
                .userPresenceEnabled()
                .enableBackgroundSync()
                .notificationConfig(chatNotificationHandler.getConfig()).build();

        // create a copy ChatUX implementation for backward compat
        ChatUI.Builder uxBuilder = new ChatUI.Builder(client(), domain, context).withFonts(chatFonts).withMarkdown(markdown).withUrlSigner(urlSigner).withStrings(getStrings());

        if (navigationHandler != null) {
            uxBuilder.withNavigationHandler(navigationHandler);
        }
        uxBuilder.build();

        client.setUser(user, userToken, new InitConnectionListener() {
            @Override
            public void onSuccess(@NotNull ConnectionData data) {
                callbacks.onSuccess(data);
            }

            @Override
            public void onError(@NotNull ChatError error) {
                callbacks.onError(error);
            }
        });

        init();
    }

    @Override
    public void disconnect() {
        ChatClient.instance().disconnect();
        disconnectChatDomainIfAlreadyInitialized();
    }

    private void disconnectChatDomainIfAlreadyInitialized() {

        if (!ChatDomain.Companion.isInitialized()) {
            ChatLogger.Companion.getInstance().logD("ChatImpl", "ChatDomain was not initialized yet. No need to disconnect.");
            return;
        }

        final ChatDomain chatDomain = ChatDomain.instance();
        BuildersKt.launch(GlobalScope.INSTANCE,
                Dispatchers.getIO(),
                CoroutineStart.DEFAULT,
                (scope, continuation) -> chatDomain.disconnect(continuation));
    }

    protected void init() {
        initSocketListener();
    }


    private void initSocketListener() {
        client().addSocketListener(new ChatSocketListener(
                onlineStatus -> {
                    ChatImpl.this.onlineStatus.postValue(onlineStatus);
                    return Unit.INSTANCE;
                },
                user -> {
                    currentUser.postValue(user);
                    return Unit.INSTANCE;
                },
                newUnreadMessages -> {
                    unreadMessages.postValue(newUnreadMessages);
                    return Unit.INSTANCE;
                },
                newUnreadChannels -> {
                    unreadChannels.postValue(newUnreadChannels);
                    return Unit.INSTANCE;
                }
        ));
    }

    private ChatClient client() {
        return ChatClient.instance();
    }
}
