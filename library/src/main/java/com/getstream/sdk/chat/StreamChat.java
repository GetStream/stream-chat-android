package com.getstream.sdk.chat;

import android.content.Context;
import android.util.Log;

import com.getstream.sdk.chat.channels.ChannelsRepository;
import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.media.MediaLoader;
import com.getstream.sdk.chat.media.MediaLoaderImpl;
import com.getstream.sdk.chat.messages.MessagesRepository;
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
import com.getstream.sdk.chat.users.UsersRepository;
import com.getstream.sdk.chat.utils.strings.StringsProvider;
import com.getstream.sdk.chat.utils.strings.StringsProviderImpl;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;

public class StreamChat {
    private static final String TAG = StreamChat.class.getSimpleName();

    private static Client INSTANCE;

    //private static MutableLiveData<OnlineStatus> onlineStatus;
    //private static MutableLiveData<Number> totalUnreadMessages;
    //private static MutableLiveData<Number> unreadChannels;
    //private static MutableLiveData<User> currentUser;
    //private static Integer lastTotalUnreadMessages;
    //private static Integer lastUnreadChannels;
    private static boolean lifecycleStopped;
    //private static boolean userWasInitialized;
    private static Context context;
    private static StringsProvider stringsProvider;
    private static StreamChatStyle chatStyle = new StreamChatStyle.Builder().build();
    private static FontsManager fontsManager;
    private static UsersRepository usersRepository;
    private static MessagesRepository messagesRepository;
    private static ChannelsRepository channelsRepository;
    private static MediaLoader mediaLoader;

//    public static LiveData<OnlineStatus> getOnlineStatus() {
//        return onlineStatus;
//    }

    /**
     * @return LiveData object for the total unread messages
     */
//    public static LiveData<Number> getTotalUnreadMessages() {
//        return totalUnreadMessages;
//    }

    /**
     * @return LiveData object for the number of unread channels
     */
//    public static LiveData<Number> getUnreadChannels() {
//        return unreadChannels;
//    }

    /**
     * @return LiveData object for the current user
     */
    //public static LiveData<User> getCurrentUser() {
    //    return currentUser;
    //}
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

//    private static void handleConnectedUser() {
//        INSTANCE.onSetUserCompleted(new ClientConnectionCallback() {
//            @Override
//            public void onSuccess(User user) {
//                userWasInitialized = true;
//                onlineStatus.postValue(OnlineStatus.CONNECTED);
//            }
//
//            @Override
//            public void onError(String errMsg, int errCode) {
//                Log.d(TAG, "handleConnectedUser: error: " + errMsg + ":" + errCode);
//            }
//        });
//    }

//    private static synchronized void setupEventListeners() {
//        Log.i(TAG, "setupEventListeners");
//        INSTANCE.addEventHandler(new ChatEventHandler() {
//            @Override
//            public void onConnectionChanged(Event event) {
//                Log.w(TAG, "connection status changed to " + (event.getOnline() ? "online" : "offline"));
//                if (event.getOnline()) {
//                    onlineStatus.postValue(OnlineStatus.CONNECTING);
//                } else {
//                    onlineStatus.postValue(OnlineStatus.FAILED);
//                }
//                handleConnectedUser();
//            }
//
//            @Override
//            public void onConnectionRecovered(Event event) {
//                Log.w(TAG, "connection recovered!");
//                List<Channel> channels = INSTANCE.getActiveChannels();
//                if (channels == null) {
//                    Log.w(TAG, "nothing to recover");
//                } else {
//                    Log.w(TAG, channels.size() + " channels to recover!");
//                }
//                onlineStatus.postValue(OnlineStatus.CONNECTED);
//            }
//
//            @Override
//            public void onAnyEvent(Event event) {
//                ClientState state = INSTANCE.getState();
//                if (state.getTotalUnreadCount() != null) {
//                    // post the value if it changed since last time
//                    if (!state.getTotalUnreadCount().equals(lastTotalUnreadMessages)) {
//                        lastTotalUnreadMessages = state.getTotalUnreadCount();
//                        totalUnreadMessages.postValue(lastTotalUnreadMessages);
//                    }
//                    if (!state.getUnreadChannels().equals(lastUnreadChannels)) {
//                        lastUnreadChannels = state.getUnreadChannels();
//                        unreadChannels.postValue(lastUnreadChannels);
//                    }
//                    if (event.getMe() != null) {
//                        currentUser.postValue(state.getCurrentUser());
//                    }
//                }
//            }
//        });
//    }

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

    @NotNull
    public static UsersRepository getUsersRepository() {
        return usersRepository;
    }

    @NotNull
    public static MessagesRepository getMessagesRepository() {
        return messagesRepository;
    }

    @NotNull
    public static ChannelsRepository getChannelsRepository() {
        return channelsRepository;
    }

    @NotNull
    public static Client client() {
        return INSTANCE;
    }

    @NotNull
    public static MediaLoader getMediaLoader() {
        return mediaLoader;
    }

    public static synchronized boolean init(String apiKey, Context context) {
        return init(apiKey, new ApiClientOptions(), context);
    }

    public static synchronized boolean init(String apiKey, ApiClientOptions apiClientOptions, @NonNull Context context) {
        if (INSTANCE != null) {
            return true;
        }
        Log.i(TAG, "calling init");
        synchronized (Client.class) {
            if (INSTANCE == null) {

                StreamChat.context = context;
                stringsProvider = new StringsProviderImpl(context);


                fontsManager = new FontsManagerImpl(context);


                Log.i(TAG, "calling init for the first time");
                INSTANCE = new Client(apiKey, apiClientOptions, new ConnectionLiveData(StreamChat.context));
                INSTANCE.setContext(StreamChat.context);

                mediaLoader = new MediaLoaderImpl(context);

                //onlineStatus = new MutableLiveData<>(OnlineStatus.NOT_INITIALIZED);
                //currentUser = new MutableLiveData<>();
                //totalUnreadMessages = new MutableLiveData<>();
                //unreadChannels = new MutableLiveData<>();
                //handleConnectedUser();

//                INSTANCE.onSetUserCompleted(new ClientConnectionCallback() {
//                    @Override
//                    public void onSuccess(User user) {
//                        Log.i(TAG, "set user worked out well");
//                        //setupEventListeners();
//                        currentUser.postValue(user);
//
//                        new StreamLifecycleObserver(new LifecycleHandler() {
//                            @Override
//                            public void resume() {
//                                Log.i(TAG, "detected resume");
//                                if (lifecycleStopped && userWasInitialized) {
//                                    lifecycleStopped = false;
//                                    INSTANCE.reconnectWebSocket(user);
//                                }
//                            }
//
//                            @Override
//                            public void stopped() {
//                                Log.i(TAG, "detected stop");
//                                lifecycleStopped = true;
//                                if (INSTANCE != null) {
//                                    INSTANCE.disconnectWebSocket();
//                                }
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void onError(String errMsg, int errCode) {
//                        onlineStatus.postValue(OnlineStatus.FAILED);
//                    }
//                });
            }
            return true;
        }
    }

}
