package com.getstream.sdk.chat.rest.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.bumptech.glide.load.model.GlideUrl;
import com.getstream.sdk.chat.ConnectionLiveData;
import com.getstream.sdk.chat.EventSubscriberRegistry;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.ClientErrorCode;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.PaginationOptions;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.providers.*;
import com.getstream.sdk.chat.rest.interfaces.*;
import com.getstream.sdk.chat.rest.request.*;
import com.getstream.sdk.chat.rest.response.*;
import com.getstream.sdk.chat.rest.storage.BaseStorage;
import com.getstream.sdk.chat.users.UsersCache;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;

import androidx.annotation.NonNull;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.getstream.sdk.chat.storage.Sync.LOCAL_FAILED;
import static com.getstream.sdk.chat.storage.Sync.SYNCED;

public class Client {

    private static final String TAG = Client.class.getSimpleName();
    private String clientID;
    private UsersCache usersCache;

    @NotNull
    public ClientState getState() {
        return state;
    }

    public void setState(ClientState state) {
        this.state = state;
    }

    CachedTokenProvider tokenProvider;
    String cacheUserToken;
    boolean fetchingToken;

    private ClientState state;

    // Main Params
    private String apiKey;

    private ApiServiceProvider apiServiceProvider;
    private WebSocketServiceProvider webSocketServiceProvider;
    private UploadStorageProvider uploadStorageProvider;
    private Context context;
    // Client params
    //private boolean connected;

    private BaseStorage uploadStorage;
    private APIService apiService;
    private WebSocketService webSocketService;
    //private Storage storage;

    private EventSubscriberRegistry<ChatEventHandler> subRegistry;
    // registry for callbacks on the setUser connection
    private EventSubscriberRegistry<ClientConnectionCallback> connectSubRegistry;
    // endregion
    private ChatEventHandler builtinHandler =

            new ChatEventHandler() {
                @Override
                public void onAnyEvent(Event event) {
                    // if an event contains the current user update it
                    // this also captures notification.mutes_updated
                    if (event.getMe() != null) {
                        //state.setCurrentUser(event.getMe());
                    }
                    if (event.getType() == EventType.NOTIFICATION_MUTES_UPDATED) {
                        Log.i(TAG, "Mutes updated");
                    }

                    // if an event contains a user update that user
                    // handles user updates, presence changes etc.
                    if (event.getUser() != null) {
                        //state.updateUser(event.getUser());
                    }

                    // update the unread count if it is present on the event
                    if (event.getTotalUnreadCount() != null) {
                        //state.setTotalUnreadCount(event.getTotalUnreadCount().intValue());
                    }
                    if (event.getUnreadChannels() != null) {
                        //state.setUnreadChannels(event.getUnreadChannels().intValue());
                    }

                    // if an event contains an updated channel write the update
                    if (event.getChannel() != null) {
                        //state.updateUsersForChannel(event.getChannel().getChannelState());
                    }
                }

                private void updateChannelMessage(Channel channel, Event event) {
                    channel.handleMessageUpdatedOrDeleted(event);
                }

                @Override
                public void onUserWatchingStart(Channel channel, Event event) {
                    channel.handleWatcherStart(event);
                }

                @Override
                public void onUserWatchingStop(Channel channel, Event event) {
                    channel.handleWatcherStop(event);
                }

                @Override
                public void onMessageNew(Channel channel, Event event) {
                    channel.handleNewMessage(event);
                }

                @Override
                public void onMessageUpdated(Channel channel, Event event) {
                    this.updateChannelMessage(channel, event);
                }

                @Override
                public void onMessageDeleted(Channel channel, Event event) {
                    this.updateChannelMessage(channel, event);
                }

                @Override
                public void onMessageRead(Channel channel, Event event) {
                    channel.handleReadEvent(event);
                }

                @Override
                public void onReactionNew(Channel channel, Event event) {
                    this.updateChannelMessage(channel, event);
                }

                @Override
                public void onReactionDeleted(Channel channel, Event event) {
                    this.updateChannelMessage(channel, event);
                }

                @Override
                public void onChannelUpdated(Channel channel, Event event) {
                    channel.handleChannelUpdated(event.getChannel());
                }

                @Override
                public void onChannelDeleted(Channel channel, Event event) {
                    channel.handleChannelDeleted(event.getChannel());
                }

                @Override
                public void onMemberAdded(Channel channel, Event event) {
                    if (event.getMember() != null) {
                        channel.handleMemberAdded(event.getMember());
                    }
                }

                @Override
                public void onMemberUpdated(Channel channel, Event event) {
                    if (event.getMember() != null) {
                        channel.handleMemberUpdated(event.getMember());
                    }
                }

                @Override
                public void onMemberRemoved(Channel channel, Event event) {
                    if (event.getUser() != null) {
                        channel.handelMemberRemoved(event.getUser());
                    }
                }

                @Override
                public void onConnectionChanged(Event event) {
                    state.connected = event.getOnline();
                }
            };

    public Client(String apiKey,
                  ApiServiceProvider apiServiceProvider,
                  WebSocketServiceProvider webSocketServiceProvider,
                  UploadStorageProvider uploadStorageProvider,
                  ConnectionLiveData connectionLiveData) {
        this.apiKey = apiKey;
        subRegistry = new EventSubscriberRegistry();
        connectSubRegistry = new EventSubscriberRegistry<>();
        this.apiServiceProvider = apiServiceProvider;
        this.webSocketServiceProvider = webSocketServiceProvider;
        this.uploadStorageProvider = uploadStorageProvider;
        this.state = new ClientState(this);

        Log.d(TAG, "instance created: " + apiKey);

        if (connectionLiveData != null) {
            connectionLiveData.observeForever(connectionModel -> {
                if (connectionModel.getIsConnected() && !state.connected) {
                    Log.i(TAG, "fast track connection discovery: UP");
                    reconnectWebSocket(state.user);
                } else if (!connectionModel.getIsConnected() && state.connected) {
                    Log.i(TAG, "fast track connection discovery: DOWN");
                    disconnectWebSocket();
                }
            });
        }
    }

    public Client(String apiKey, ApiClientOptions options) {
        this(apiKey, new StreamApiServiceProvider(options),
                new StreamWebSocketServiceProvider(options, apiKey),
                new StreamUploadStorageProvider(options),
                null);
    }

    public Client(String apiKey, ApiClientOptions options, ConnectionLiveData connectionLiveData) {
        this(apiKey, new StreamApiServiceProvider(options),
                new StreamWebSocketServiceProvider(options, apiKey),
                new StreamUploadStorageProvider(options),
                connectionLiveData);
    }

    public String getApiKey() {
        return apiKey;
    }

//    /**
//     * Returns the current user set in client's state
//     */
//    public User getUser() {
//        return state.getCurrentUser();
//    }

    //    /**
//     * Returns the current user set in client's state
//     */
    public String getUserId() {
        if (state.user != null) return state.user.getUserId();
        else return null;
    }

    public String getClientID() {
        return clientID;
    }

    public APIService getApiService() {
        return apiService;
    }

//    public boolean isConnected() {
//        return connected;
//    }

    /**
     * The opposite of {@link #setUser(User, TokenProvider)} this closes the current WebSocket connection
     * and resets the client state as if setUser was never initialized
     * <p>
     * Calls to this method will return an error if a user was not set; if a user was set but
     * the connection is still pending (setUser is asynchronous) this method will also abort the pending
     * connection
     */
    public synchronized void disconnect() {
        if (!state.connected) {
            Log.w(TAG, "disconnect was called but setUser was not called yet");
        } else {
            Log.d(TAG, "disconnecting");
        }

        disconnectWebSocket();

        // unset token facilities
        tokenProvider = null;
        fetchingToken = false;
        cacheUserToken = null;

        builtinHandler.dispatchUserDisconnected();
        for (ChatEventHandler handler : subRegistry.getSubscribers()) {
            handler.dispatchUserDisconnected();
        }

        // clear local state
        state.reset();
    }

    public synchronized void setUser(User user, final TokenProvider provider, ClientConnectionCallback callback) {
        connectSubRegistry.addSubscription(callback);
        setUser(user, provider);
    }

    /**
     * Sets the current user for chat
     * <p>
     * 1. it sets the current user to the client
     * 2. it requests the token from the provided TokenProvider
     * 3. uses {@link #connect} to continue with the initialization process
     * <p>
     * This method is required for most of Chat SDK functionality to work; since this is an async
     * function (a WebSocket connection must be established) code that depends on the initialization
     * of the user should be not be called directly but await for setUser to be completed
     * <p>
     * Further calls to setUser are ignored; in order to change current user you first need to call
     * {@link #disconnect()}}
     *
     * @param user     the user to set as current
     * @param provider the Token Provider used to obtain the auth token for the user
     */
    public synchronized void setUser(@NotNull User user, @NotNull final TokenProvider provider) {
//        if (user == null) {
//            Log.w(TAG, "user can't be null. If you want to reset current user you need to call client.disconnect()");
//            return;
//        }
//        if (getUser() != null) {
//            Log.w(TAG, "setUser was called but a user is already set; this is probably an integration mistake");
//            return;
//        }
        Log.d(TAG, "setting user: " + user.getId());

        //state.setCurrentUser(user);
        List<TokenProvider.TokenProviderListener> listeners = new ArrayList<>();

        state.user = user;

        this.tokenProvider = new CachedTokenProvider() {
            @Override
            public void getToken(TokenProvider.TokenProviderListener listener) {
                // use the cached token if possible
                if (cacheUserToken != null) {
                    listener.onSuccess(cacheUserToken);
                    return;
                }

                // queue the listener up instead of spawning more getToken calls
                if (fetchingToken) {
                    listeners.add(listener);
                    return;
                } else {
                    // token is not in cache and there are no in-flight requests, go fetch it
                    Log.d(TAG, "Go get a new token");
                    fetchingToken = true;
                }

                provider.getToken(token -> {
                    cacheUserToken = token;
                    fetchingToken = false;
                    Log.d(TAG, "We got another token " + token);
                    listener.onSuccess(token);
                    for (TokenProvider.TokenProviderListener l :
                            listeners) {
                        l.onSuccess(token);
                    }
                    listeners.clear();
                });
            }

            @Override
            public void tokenExpired() {
                Log.d(TAG, "Current token is expired: " + cacheUserToken);
                cacheUserToken = null;
            }
        };
        connect(user);
    }

//    /**
//     * Generates a message id based on the user id + a random UUID.
//     * We generate the message client side to make it easier to update the local storage/in-memory store of messages
//     *
//     * @return a string with the new message id
//     */
//    public String generateMessageID() {
//        return getUserId() + "-" + randomUUID().toString();
//    }

    public void setUser(User user, @NonNull String token, ClientConnectionCallback callback) {
        setUser(user, listener -> listener.onSuccess(token), callback);
    }

    public void setUser(User user, @NonNull String token) {
        setUser(user, listener -> listener.onSuccess(token));
    }

    //public boolean fromCurrentUser(UserEntity entity) {
    //    String otherUserId = entity.getUserId();
    //    if (otherUserId == null) return false;
    //    if (state.user == null) return false;
    //    return TextUtils.equals(getUserId(), otherUserId);
    //}

    /**
     * Creates an authorization token for development purposes.
     * Note: you must configure your application to disable auth checks
     * This only suitable for a dev/test environment.
     *
     * @param userId the id of the user
     * @return the dev token for the user
     */
    public String devToken(@NonNull String userId) {
        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("User ID must be non-null");
        }

        String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"; //  {"alg": "HS256", "typ": "JWT"}
        JSONObject payloadJson = new JSONObject();

        try {
            payloadJson.put("user_id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String payload = payloadJson.toString();
        String payloadBase64 = Base64.encodeToString(payload.getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        String devSignature = "devtoken";

        String[] a = new String[3];
        a[0] = header;
        a[1] = payloadBase64;
        a[2] = devSignature;

        return TextUtils.join(".", a);
    }

    /**
     * Event Delegation: Adds an event handler for client events received via WebSocket
     *
     * @param handler the event handler for client events
     * @return the identifier of the handler, you can use that to remove it, see: {@link #removeEventHandler(Integer)}
     */
    public final int addEventHandler(ChatEventHandler handler) {
        Integer subID = subRegistry.addSubscription(handler);
        return subID;
    }

    /**
     * Event Delegation: removes an event handler via its id
     * <p>
     * Removing an handler that was not registered is a no-op
     *
     * @param handlerId the event handler for client events
     */
    public final void removeEventHandler(Integer handlerId) {
        subRegistry.removeSubscription(handlerId);
    }

    public void setUsersCache(UsersCache usersCache) {

        this.usersCache = usersCache;
    }

//    /**
//     * Makes sure the callback is called when the user is ready
//     * <p>
//     * If the user is setup, it will run immediately; otherwise it will be added to a
//     * waiting list and will be fired as soon as the user is ready (see {@link #setUser(User, TokenProvider)} for more)
//     *
//     * @param callback the callback to run when
//     */
//    public synchronized void onSetUserCompleted(ClientConnectionCallback callback) {
//        if (connected) {
//            callback.onSuccess(getUser());
//        } else {
//            connectSubRegistry.addSubscription(callback);
//        }
//    }

    private void onWSEvent(Event event) {
        builtinHandler.dispatchEvent(Client.this, event);
        for (ChatEventHandler handler : subRegistry.getSubscribers()) {
            handler.dispatchEvent(Client.this, event);
        }
    }

    private synchronized void connect(@NotNull User user) {
        Log.i(TAG, "client.connect was called");
        tokenProvider.getToken(userToken -> {
            try {
                WSResponseHandler wsHandler = new WSResponseHandler() {
                    @Override
                    public void onWSEvent(Event event) {
                        Client.this.onWSEvent(event);
                    }

                    @Override
                    public void connectionResolved(Event event) {
                        clientID = event.getConnectionId();

                        //if (event.getMe() != null)
                        //    state.setCurrentUser(event.getMe());

                        // mark as connect, any new callbacks will automatically be executed
                        state.connected = true;
                        usersCache.setCurrentUser(user);

                        // call onSuccess for everyone that was waiting
                        List<ClientConnectionCallback> subs = connectSubRegistry.getSubscribers();
                        connectSubRegistry.clear();
                        for (ClientConnectionCallback waiter : subs) {
                            waiter.onSuccess(user);
                        }
                    }

                    @Override
                    public void connectionRecovered() {
                        onWSEvent(new Event(EventType.CONNECTION_RECOVERED.label));
                    }

                    @Override
                    public void tokenExpired() {
                        tokenProvider.tokenExpired();
                        disconnectWebSocket();
                        reconnectWebSocket(state.user);
                    }

                    @Override
                    public void onError(WsErrorMessage error) {
                        Client.this.onError(error.getError().getMessage(), error.getError().getCode());
                    }
                };
                webSocketService = webSocketServiceProvider.provideWebSocketService(user, userToken, wsHandler);
                apiService = apiServiceProvider.provideApiService(tokenProvider);
                uploadStorage = uploadStorageProvider.provideUploadStorage(tokenProvider, this);
                webSocketService.connect();
            } catch (UnsupportedEncodingException e) {
                onError(e.getMessage(), ClientErrorCode.JSON_ENCODING);
            }
        });
    }

    private void onError(String errMsg, int errCode) {
        List<ClientConnectionCallback> subs = connectSubRegistry.getSubscribers();
        connectSubRegistry.clear();
        for (ClientConnectionCallback waiter : subs) {
            waiter.onError(errMsg, errCode);
        }
    }

    /**
     * the opposite of {@link #disconnectWebSocket()}
     */
    public void reconnectWebSocket(User user) {
//        if (getUser() == null) {
//            Log.w(TAG, "calling reconnectWebSocket before setUser is a no-op");
//            return;
//        }
//        if (webSocketService != null) {
//            Log.w(TAG, "tried to reconnectWebSocket by a connection is still set");
//            return;
//        }
        //connectionRecovered();
        connect(user);
    }

    // endregion

    // region Channel
    public void queryChannels(QueryChannelsRequest request, QueryChannelListCallback callback) {
        String payload = GsonConverter.Gson().toJson(request);

        apiService.queryChannels(apiKey, state.getUserId(), clientID, payload).enqueue(new Callback<QueryChannelsResponse>() {
            @Override
            public void onResponse(Call<QueryChannelsResponse> call, Response<QueryChannelsResponse> response) {

                for (ChannelState channelState : response.body().getChannelStates()) {

                    if (channelState.getLastMessage() != null)
                        channelState.getLastMessage().setSyncStatus(SYNCED);
                    Channel channel = channelState.getChannel();

                    channel.setClient(Client.this);
                    channel.setLastState(channelState);

                    channel.mergeWithState(channelState);
                    if (request.isWatch()) {
                        channel.setInitialized(true);
                    }

                    // update the user references
                    //state.updateUsersForChannel(channelState);
                }

                // store the results of the query
                //QueryChannelsQ query = request.query();
                //List<Channel> channels = response.body().getChannels();

                // callback
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<QueryChannelsResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * edit the channel's custom properties.
     *
     * @param channel       the channel needs to update
     * @param updateMessage message allowing you to show a system message in the Channel that something changed
     * @param callback      the result callback
     */
    public void updateChannel(@NonNull Channel channel, @Nullable Message updateMessage, @NotNull ChannelCallback callback) {
        apiService.updateChannel(channel.getType(), channel.getId(), apiKey, clientID,
                new UpdateChannelRequest(channel.getExtraData(), updateMessage))
                .enqueue(new Callback<ChannelResponse>() {
                    @Override
                    public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * removes the channel. Messages are permanently removed.
     *
     * @param channel  the channel needs to delete
     * @param callback the result callback
     */
    public void deleteChannel(@NonNull Channel channel, @NotNull ChannelCallback callback) {
        apiService.deleteChannel(channel.getType(), channel.getId(), apiKey, clientID)
                .enqueue(new Callback<ChannelResponse>() {
                    @Override
                    public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * stops watching the channel for events.
     *
     * @param channel  stops watch this channel
     * @param callback the result callback
     */
    public void stopWatchingChannel(@NotNull Channel channel, @NotNull CompletableCallback callback) {
        apiService.stopWatching(channel.getType(), channel.getId(), apiKey, clientID, Collections.emptyMap())
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * Query the API, get messages, members or other channel fields
     *
     * @param channel  query data for this channel
     * @param request  request options
     * @param callback the result callback
     */
    public void queryChannel(Channel channel, ChannelQueryRequest request, QueryChannelCallback callback) {
        final ChannelQueryRequest queryRequest = request.withData(channel.getExtraData());
        Callback<ChannelState> requestCallback = new Callback<ChannelState>() {
            @Override
            public void onResponse(Call<ChannelState> call, Response<ChannelState> response) {
                Log.i(TAG, "channel query: incoming watchers " + response.body().getWatchers().size());
                channel.mergeWithState(response.body());
                // channels created without ID will get it populated by the API
                if (channel.getId() == null) {
                    channel.setId(response.body().getChannel().getCid().split(":")[1]);
                    channel.setCid(response.body().getChannel().getCid());
                }
                if (channel.getConfig() == null) {
                    channel.setConfig(response.body().getChannel().getConfig());
                }
                if (channel.getChannelState() == null) {
                    channel.setChannelState(response.body());
                }
                if (channel.getCreatedByUser() == null) {
                    channel.setCreatedByUser(response.body().getChannel().getCreatedByUser());
                }

                if (queryRequest.isWatch()) {
                    channel.setInitialized(true);
                }

                // update the user references
                //getState().updateUsersForChannel(channel.getChannelState());

                Log.i(TAG, "channel query: merged watchers " + channel.getChannelState().getWatchers().size());
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<ChannelState> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        };
        if (channel.getId() == null) {
            // channels created without ID will get it populated by the API
            apiService.queryChannel(channel.getType(), apiKey, state.getUserId(), clientID,
                    queryRequest).enqueue(requestCallback);
        } else {
            apiService.queryChannel(channel.getType(), channel.getId(), apiKey, state.getUserId(),
                    clientID, queryRequest).enqueue(requestCallback);
        }
    }

    /**
     * hides the channel from queryChannels for the user until a message is added TODO: track hidden state in Room
     *
     * @param channel  the channel needs to hide
     * @param callback the result callback
     */
    public void hideChannel(@NonNull Channel channel, @NotNull CompletableCallback callback) {
        apiService.hideChannel(channel.getType(), channel.getId(), apiKey, clientID, Collections.EMPTY_MAP)
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * removes the hidden status for a channel TODO: track hidden state in Room
     *
     * @param channel  the channel needs to show
     * @param callback the result callback
     */
    public void showChannel(@NonNull Channel channel, @NotNull CompletableCallback callback) {
        apiService.showChannel(channel.getType(), channel.getId(), apiKey, clientID, Collections.EMPTY_MAP)
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * Accept an invite to the channel
     *
     * @param channel  accept invite to this channel
     * @param message  message object allowing you to show a system message in the Channel
     * @param callback the result callback
     */
    public void acceptInvite(@NotNull Channel channel, @Nullable String message, @NotNull ChannelCallback callback) {
        apiService.acceptInvite(channel.getType(), channel.getId(), apiKey, clientID, new AcceptInviteRequest(message))
                .enqueue(new Callback<ChannelResponse>() {
                    @Override
                    public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<ChannelResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * Reject an invite to the channel
     *
     * @param channel  reject invite to this channel
     * @param callback the result callback
     */
    public void rejectInvite(@NotNull Channel channel, @NotNull ChannelCallback callback) {
        apiService.rejectInvite(channel.getType(), channel.getId(), apiKey, clientID, new RejectInviteRequest())
                .enqueue(new Callback<ChannelResponse>() {
                    @Override
                    public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<ChannelResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }


    // region Message

    /**
     * sendMessage - Sends a message to a channel
     *
     * @param channel The channel
     * @param message The message
     */
    public void sendMessage(Channel channel,
                            @NonNull Message message,
                            MessageCallback callback) {

        String str = GsonConverter.Gson().toJson(message);
        Map<String, Object> map = new HashMap<>();
        map.put("message", GsonConverter.Gson().fromJson(str, Map.class));

        apiService.sendMessage(channel.getType(), channel.getId(), apiKey, state.getUserId(), clientID, map).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                message.setSyncStatus(SYNCED);
                if (response.body() != null && response.body().getMessage() != null) {
                    response.body().getMessage().setSyncStatus(SYNCED);
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(StreamChat.getContext().getString(R.string.stream_message_invalid_response), -1);
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                message.setSyncStatus(LOCAL_FAILED);
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * Updates a message
     *
     * @param message The Message object
     */
    public void updateMessage(@NonNull Message message,
                              MessageCallback callback) {

        String str = GsonConverter.Gson().toJson(message);
        Map<String, Object> map = new HashMap<>();
        map.put("message", GsonConverter.Gson().fromJson(str, Map.class));
        apiService.updateMessage(message.getId(),
                apiKey,
                state.getUserId(),
                clientID,
                map).enqueue(new Callback<MessageResponse>() {

            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                message.setSyncStatus(SYNCED);
                if (response.body() != null && response.body().getMessage() != null)
                    response.body().getMessage().setSyncStatus(SYNCED);
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                message.setSyncStatus(LOCAL_FAILED);
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    public void getMessage(@NonNull String messageId,
                           MessageCallback callback) {

        apiService.getMessage(messageId, apiKey, state.getUserId(), clientID).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * Deletes a message
     *
     * @param messageId the id of the message to delete
     * @param callback  the result callback
     */
    public void deleteMessage(@NonNull String messageId,
                              MessageCallback callback) {

        apiService.deleteMessage(messageId, apiKey, state.getUserId(), clientID).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * Marks a channel as read for this user, only works if the `read_events` setting is enabled
     *
     * @param channel     the channel to mark as read
     * @param readRequest the mark read request additional options
     * @param callback    the result callback
     */
    public void markRead(@NonNull Channel channel,
                         MarkReadRequest readRequest,
                         EventCallback callback) {

        apiService.markRead(channel.getType(), channel.getId(), apiKey, state.getUserId(), clientID, readRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });

    }

    /**
     * search messages by parameters
     *
     * @param request  request options include filter, query string and query options
     * @param callback the result callback
     */
    public void searchMessages(@NotNull SearchMessagesRequest request, @NotNull SearchMessagesCallback callback) {
        String requestString = GsonConverter.Gson().toJson(request);
        apiService.searchMessages(apiKey, clientID, requestString)
                .enqueue(new Callback<SearchMessagesResponse>() {
                    @Override
                    public void onResponse(Call<SearchMessagesResponse> call, Response<SearchMessagesResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<SearchMessagesResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    // endregion

    // region Thread

    /**
     * Marks all channels for this user as read
     *
     * @param callback the result callback
     */
    public void markAllRead(EventCallback callback) {

        apiService.markAllRead(apiKey, state.getUserId(), clientID).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }
    // endregion

    // region Reaction

    /**
     * Lists the message replies for a parent message
     *
     * @param parentId the id of the parent message
     * @param limit    the number of messages to retrieve older than idLt
     * @param idLt     the id of the reply to use as offset (if null or empty it will fetch replies from the oldest)
     * @param callback the result callback
     */
    public void getReplies(@NonNull String parentId,
                           int limit,
                           String idLt,
                           GetRepliesCallback callback) {

        if (TextUtils.isEmpty(idLt)) {
            apiService.getReplies(parentId, apiKey, state.getUserId(), clientID, limit).enqueue(new Callback<GetRepliesResponse>() {
                @Override
                public void onResponse(Call<GetRepliesResponse> call, Response<GetRepliesResponse> response) {
                    callback.onSuccess(response.body());
                }

                @Override
                public void onFailure(Call<GetRepliesResponse> call, Throwable t) {
                    if (t instanceof ErrorResponse) {
                        callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                    } else {
                        callback.onError(t.getLocalizedMessage(), -1);
                    }
                }
            });
        } else {
            apiService.getRepliesMore(parentId, apiKey, state.getUserId(), clientID, limit, idLt).enqueue(new Callback<GetRepliesResponse>() {
                @Override
                public void onResponse(Call<GetRepliesResponse> call, Response<GetRepliesResponse> response) {
                    callback.onSuccess(response.body());
                }

                @Override
                public void onFailure(Call<GetRepliesResponse> call, Throwable t) {
                    if (t instanceof ErrorResponse) {
                        callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                    } else {
                        callback.onError(t.getLocalizedMessage(), -1);
                    }
                }
            });
        }

    }

    /**
     * Sends a reaction about a message
     *
     * @param callback the result callback
     */
    public void sendReaction(@NotNull ReactionRequest reactionRequest,
                             @NotNull MessageCallback callback) {


        apiService.sendReaction(reactionRequest.getReaction().getMessageId(), apiKey, state.getUserId(), clientID, reactionRequest).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    // endregion

    // region Event

    /**
     * Deletes a reaction by user and type
     *
     * @param messageId the message id
     * @param type      the type of reaction that should be removed
     * @param callback  the result callback
     */
    public void deleteReaction(@NonNull String messageId,
                               @NonNull String type,
                               MessageCallback callback) {

        apiService.deleteReaction(messageId, type, apiKey, state.getUserId(), clientID).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * list the reactions, supports pagination
     *
     * @param messageId  the message id
     * @param pagination pagination options
     * @param callback   the result callback
     */
    public void getReactions(@NotNull String messageId,
                             @NotNull PaginationOptions pagination,
                             @NotNull GetReactionsCallback callback) {
        apiService.getReactions(messageId, apiKey, clientID, pagination.getLimit(), pagination.getOffset())
                .enqueue(new Callback<GetReactionsResponse>() {
                    @Override
                    public void onResponse(Call<GetReactionsResponse> call, Response<GetReactionsResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<GetReactionsResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * list of reactions (10 most recent reactions)
     *
     * @param messageId the message id
     * @param callback  the result callback
     */
    public void getReactions(@NotNull String messageId,
                             @NotNull GetReactionsCallback callback) {
        getReactions(messageId, new PaginationOptions.Builder().limit(10).build(), callback);
    }

    // endregion

    /**
     * Sends an event on a channel
     *
     * @param channel      the channel for this event
     * @param eventRequest the send event request
     * @param callback     the result callback
     */
    public void sendEvent(@NonNull Channel channel,
                          @NonNull SendEventRequest eventRequest,
                          EventCallback callback) {

        apiService.sendEvent(channel.getType(), channel.getId(), apiKey, state.getUserId(), clientID, eventRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }


    // region User

    /**
     * Bans target user ID
     *
     * @param targetUserId the ID of the user to ban
     * @param channel      ban the user for this channel. If channel == null - ban the user from all channels
     * @param reason       the reason the ban was created.
     * @param timeout      the timeout in minutes until the ban is automatically expired.
     * @param callback     the result callback
     */
    public void banUser(@NotNull String targetUserId, @Nullable Channel channel,
                        @Nullable String reason, @Nullable Integer timeout,
                        @NotNull CompletableCallback callback) {
        apiService.banUser(apiKey, clientID,
                new BanUserRequest(targetUserId, timeout, reason,
                        channel != null ? channel.getType() : null,
                        channel != null ? channel.getId() : null))
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * Removes the ban for target user ID
     *
     * @param targetUserId the ID of the user to remove the ban
     * @param channel      ban the user for this channel. If channel == null - revoke global ban for a user
     * @param callback     the result callback
     */
    public void unBanUser(@NotNull String targetUserId, @Nullable Channel channel,
                          @NotNull CompletableCallback callback) {
        apiService.unBanUser(apiKey, clientID, targetUserId,
                channel != null ? channel.getType() : null,
                channel != null ? channel.getId() : null)
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * search for users and see if they are online/offline
     *
     * @param request  request options include filter, sort options and query options
     * @param callback the result callback
     */
    public void queryUsers(QueryUserRequest request,
                           QueryUserListCallback callback) {
        String requestString = GsonConverter.Gson().toJson(request);
        apiService.queryUsers(apiKey, clientID, requestString)
                .enqueue(new Callback<QueryUserListResponse>() {
                    @Override
                    public void onResponse(Call<QueryUserListResponse> call, Response<QueryUserListResponse> response) {
                        //state.updateUsers(response.body().getUsers());
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<QueryUserListResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });

    }

    /**
     * Setup an anonymous session
     */
    public void setAnonymousUser() {
    }

    /**
     * Setup a temporary guest user
     */
    public void setGuestUser(User user) {
    }

    /**
     * Mutes a user
     *
     * @param target_id the id of the user to mute
     * @param callback  the result callback
     */
    public void muteUser(@NonNull String target_id,
                         MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id", target_id);
        body.put("user_id", state.getUserId());

        apiService.muteUser(apiKey, state.getUserId(), clientID, body).enqueue(new Callback<MuteUserResponse>() {
            @Override
            public void onResponse(Call<MuteUserResponse> call, Response<MuteUserResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<MuteUserResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * Unmutes another user, the reverse of {@link #muteUser}
     *
     * @param target_id the id of the user to un-mute
     * @param callback  the result callback
     */
    public void unmuteUser(@NonNull String target_id,
                           MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id", target_id);
        body.put("user_id", state.getUserId());

        apiService.unMuteUser(apiKey, state.getUserId(), clientID, body).enqueue(new Callback<MuteUserResponse>() {
            @Override
            public void onResponse(Call<MuteUserResponse> call, Response<MuteUserResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<MuteUserResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    public void flagUser(@NonNull String targetUserId,
                         FlagCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_user_id", targetUserId);

        apiService.flag(apiKey, state.getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
            @Override
            public void onResponse(Call<FlagResponse> call, Response<FlagResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<FlagResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    public void unFlagUser(@NonNull String targetUserId,
                           FlagCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_user_id", targetUserId);

        apiService.unFlag(apiKey, state.getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
            @Override
            public void onResponse(Call<FlagResponse> call, Response<FlagResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<FlagResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * Adds members with given user IDs to the channel
     *
     * @param channel  add members to this channel
     * @param members  list of user IDs to add as members
     * @param callback the result callback
     */
    public void addMembers(@NotNull Channel channel, @NotNull List<String> members,
                           @NotNull ChannelCallback callback) {
        apiService.addMembers(channel.getType(), channel.getId(), apiKey, clientID, new AddMembersRequest(members))
                .enqueue(new Callback<ChannelResponse>() {
                    @Override
                    public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<ChannelResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    /**
     * Removes members with given user IDs from the channel
     *
     * @param channel  remove members to this channel
     * @param members  list of user IDs to remove from the member list
     * @param callback the result callback
     */
    public void removeMembers(@NotNull Channel channel, @NotNull List<String> members,
                              @NotNull ChannelCallback callback) {
        apiService.removeMembers(channel.getType(), channel.getId(), apiKey, clientID, new RemoveMembersRequest(members))
                .enqueue(new Callback<ChannelResponse>() {
                    @Override
                    public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<ChannelResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    // endregion
    public void sendAction(@NonNull String messageId,
                           @NonNull SendActionRequest request,
                           MessageCallback callback) {

        apiService.sendAction(messageId, apiKey, state.getUserId(), clientID, request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }


    // region Device

    /**
     * Adds a push device for a user.
     *
     * @param deviceId the id of the device to add
     * @param callback the result callback
     */
    public void addDevice(@NonNull String deviceId,
                          CompletableCallback callback) {
        AddDeviceRequest request = new AddDeviceRequest(deviceId);
        apiService.addDevices(apiKey, state.getUserId(), clientID, request).enqueue(new Callback<CompletableResponse>() {
            @Override
            public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<CompletableResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * Returns the devices associated with a current user
     */
    public void getDevices(GetDevicesCallback callback) {

        apiService.getDevices(apiKey, state.getUserId(), clientID).enqueue(new Callback<GetDevicesResponse>() {
            @Override
            public void onResponse(Call<GetDevicesResponse> call, Response<GetDevicesResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<GetDevicesResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * removeDevice - Removes the device with the given id. Clientside users can only delete their own devices
     */
    public void removeDevice(@NonNull String deviceId,
                             CompletableCallback callback) {
        apiService.deleteDevice(deviceId, apiKey, state.getUserId(), clientID).enqueue(new Callback<CompletableResponse>() {
            @Override
            public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<CompletableResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    /**
     * closes the WebSocket connection and sends a connection.change event to all listeners
     */
    public synchronized void disconnectWebSocket() {
        Log.i(TAG, "disconnecting websocket");
        if (webSocketService != null) {
            webSocketService.disconnect();
            webSocketService = null;
            clientID = null;
        }
        onWSEvent(new Event(false));
        state.connected = false;
    }

    public void flagMessage(@NonNull String targetMessageId,
                            FlagCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_message_id", targetMessageId);

        apiService.flag(apiKey, state.getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
            @Override
            public void onResponse(Call<FlagResponse> call, Response<FlagResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<FlagResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    public void unFlagMessage(@NonNull String targetMessageId,
                              FlagCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_message_id", targetMessageId);

        apiService.unFlag(apiKey, state.getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
            @Override
            public void onResponse(Call<FlagResponse> call, Response<FlagResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<FlagResponse> call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            }
        });
    }

    public void sendFile(Channel channel, File file, String mimeType, UploadFileCallback callback) {
        uploadStorage.sendFile(channel, file, mimeType, callback);
    }

    public void deleteFile(@NotNull Channel channel, @NotNull String url, @NotNull CompletableCallback callback) {
        uploadStorage.deleteFile(channel, url, callback);
    }

    public void deleteImage(@NotNull Channel channel, @NotNull String url, @NotNull CompletableCallback callback) {
        uploadStorage.deleteImage(channel, url, callback);
    }

    public String signFileUrl(String url) {
        return uploadStorage.signFileUrl(url);
    }

    public GlideUrl signGlideUrl(String url) {
        return uploadStorage.signGlideUrl(url);
    }

    public void setContext(Context context) {
        this.context = context;
    }

//    public Context getContext() {
//        return context;
//    }

//    public BaseStorage getUploadStorage() {
//        return uploadStorage;
//    }
}