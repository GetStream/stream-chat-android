package com.getstream.sdk.chat;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.ChatEventHandler;
import com.getstream.sdk.chat.rest.core.Client;

import java.util.List;

public class StreamChat {
    private static final String TAG = StreamChat.class.getSimpleName();

    private static Client INSTANCE;

    private static MutableLiveData<OnlineStatus> onlineStatus;
    private static MutableLiveData<Number> totalUnreadMessages;
    private static MutableLiveData<Number> unreadChannels;
    private static boolean lifecycleStopped;
    private static boolean userWasInitialized;

    public static LiveData<OnlineStatus> getOnlineStatus() {
        return onlineStatus;
    }

    public static LiveData<Number> getTotalUnreadMessages() {
        return totalUnreadMessages;
    }

    public static LiveData<Number> getUnreadChannels() {
        return unreadChannels;
    }

    public static synchronized Client getInstance(final Context context) {
        if (INSTANCE == null) {
            throw new RuntimeException("You must initialize the API client first, make sure to call StreamChat.initialize");
        } else {
            return INSTANCE;
        }
    }

    private static void handleConnectedUser() {
        INSTANCE.onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                userWasInitialized = true;
                onlineStatus.postValue(OnlineStatus.CONNECTED);
                totalUnreadMessages.postValue(user.getTotalUnreadCount());
                unreadChannels.postValue(user.getUnreadChannels());
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        });
    }

    private static synchronized void setupEventListeners() {
        Log.i(TAG, "setupEventListeners");
        INSTANCE.addEventHandler(new ChatEventHandler() {
            @Override
            public void onConnectionChanged(Event event) {
                Log.w(TAG, "connection status changed to " + (event.getOnline() ? "online" : "offline"));
                if (event.getOnline()) {
                    onlineStatus.postValue(OnlineStatus.CONNECTING);
                } else {
                    onlineStatus.postValue(OnlineStatus.FAILED);
                }
                handleConnectedUser();
            }

            @Override
            public void onConnectionRecovered(Event event) {
                Log.w(TAG, "connection recovered!");
                List<Channel> channels = INSTANCE.getActiveChannels();
                if (channels == null) {
                    Log.w(TAG, "nothing to recover");
                } else {
                    Log.w(TAG, channels.size() + " channels to recover!");
                }
                onlineStatus.postValue(OnlineStatus.CONNECTED);
            }

            @Override
            public void onAnyEvent(Event event) {
                if (event.getTotalUnreadCount() != null) {
                    totalUnreadMessages.postValue(event.getTotalUnreadCount());
                }
                if (event.getUnreadChannels() != null) {
                    unreadChannels.postValue(event.getUnreadChannels());
                }
            }
        });
    }

    public static synchronized boolean init(String apiKey, Context context) {
        return init(apiKey, new ApiClientOptions(), context);
    }

    public static synchronized boolean init(String apiKey, ApiClientOptions apiClientOptions, Context context) {
        if (INSTANCE != null) {
            throw new RuntimeException("StreamChat is already initialized!");
        }
        Log.i(TAG, "calling init");
        synchronized (Client.class) {
            if (INSTANCE == null) {
                Log.i(TAG, "calling init for the first time");
                INSTANCE = new Client(apiKey, apiClientOptions, new ConnectionLiveData(context));
                INSTANCE.setContext(context);
                onlineStatus = new MutableLiveData<>(OnlineStatus.NOT_INITIALIZED);
                totalUnreadMessages = new MutableLiveData<>();
                unreadChannels = new MutableLiveData<>();
                handleConnectedUser();

                INSTANCE.onSetUserCompleted(new ClientConnectionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        Log.i(TAG, "set user worked out well");
                        setupEventListeners();

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

                    @Override
                    public void onError(String errMsg, int errCode) {
                        onlineStatus.postValue(OnlineStatus.FAILED);
                    }
                });
            }
        }
        return true;
    }
}
