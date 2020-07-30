package com.getstream.sdk.chat.rest.core;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.ConnectionLiveData;
import com.getstream.sdk.chat.EventSubscriberRegistry;
import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.enums.ClientErrorCode;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Config;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.PaginationOptions;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.core.providers.ApiServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.StorageProvider;
import com.getstream.sdk.chat.rest.core.providers.StreamApiServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.StreamStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.StreamUploadStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.StreamWebSocketServiceProvider;
import com.getstream.sdk.chat.rest.core.providers.UploadStorageProvider;
import com.getstream.sdk.chat.rest.core.providers.WebSocketServiceProvider;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.GetDevicesCallback;
import com.getstream.sdk.chat.rest.interfaces.GetReactionsCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.MuteUserCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.interfaces.SearchMessagesCallback;
import com.getstream.sdk.chat.rest.request.AcceptInviteRequest;
import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.AddMembersRequest;
import com.getstream.sdk.chat.rest.request.BanUserRequest;
import com.getstream.sdk.chat.rest.request.ChannelQueryRequest;
import com.getstream.sdk.chat.rest.request.GuestUserRequest;
import com.getstream.sdk.chat.rest.request.HideChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.request.QueryUserRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.RejectInviteRequest;
import com.getstream.sdk.chat.rest.request.RemoveMembersRequest;
import com.getstream.sdk.chat.rest.request.SearchMessagesRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.UpdateChannelRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FlagResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetReactionsResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.rest.response.MuteUserResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.rest.response.SearchMessagesResponse;
import com.getstream.sdk.chat.rest.response.TokenResponse;
import com.getstream.sdk.chat.rest.response.WsErrorMessage;
import com.getstream.sdk.chat.rest.storage.BaseStorage;
import com.getstream.sdk.chat.storage.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.in;
import static com.getstream.sdk.chat.storage.Sync.LOCAL_FAILED;
import static com.getstream.sdk.chat.storage.Sync.SYNCED;
import static java.util.UUID.randomUUID;

public class Client implements WSResponseHandler {

    private static final String TAG = Client.class.getSimpleName();
    private String clientID;

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

    private ApiClientOptions apiClientOptions;
    private Boolean offlineStorage;
    private boolean anonymousConnection;
    private ApiServiceProvider apiServiceProvider;
    private WebSocketServiceProvider webSocketServiceProvider;
    private UploadStorageProvider uploadStorageProvider;
    private StorageProvider storageProvider;
    private Context context;
    private Handler delayedDisconnectWebSocketHandler = new Handler();


    // Client params
    private Map<String, Channel> activeChannelMap = new HashMap<>();
    private boolean connected;

    private BaseStorage uploadStorage;
    private APIService apiService;
    private WebSocketService webSocketService;
    private Storage storage;

    private EventSubscriberRegistry<ChatEventHandler> subRegistry;
    // registry for callbacks on the setUser connection
    private EventSubscriberRegistry<ClientConnectionCallback> connectSubRegistry;

    private static int defaultWebSocketDelay = 1000 * 10; // milliseconds * seconds
    private int webSocketDisconnectDelay;

    private Map<String, Config> channelTypeConfigs;

    // endregion
    private ChatEventHandler builtinHandler =

            new ChatEventHandler() {
                @Override
                public void onAnyEvent(Event event) {
                    // if an event contains the current user update it
                    // this also captures notification.mutes_updated
                    if (event.getMe() != null && !event.isAnonymous()) {
                        state.setCurrentUser(event.getMe());
                    }
                    if (event.getType() == EventType.NOTIFICATION_MUTES_UPDATED) {
                        StreamChat.getLogger().logI(this, "Mutes updated");
                    }

                    // if an event contains a user update that user
                    // handles user updates, presence changes etc.
                    if (event.getUser() != null) {
                        state.updateUser(event.getUser());
                    }

                    // update the unread count if it is present on the event
                    if (event.getTotalUnreadCount() != null) {
                        state.setTotalUnreadCount(event.getTotalUnreadCount().intValue());
                    }
                    if (event.getUnreadChannels() != null) {
                        state.setUnreadChannels(event.getUnreadChannels().intValue());
                    }

                    // if an event contains an updated channel write the update
                    if (event.getChannel() != null) {
                        state.updateUsersForChannel(event.getChannel().getChannelState());
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
                    if (!event.getOnline()) {
                        connected = false;
                    }
                }
            };

    public Client(String apiKey,
                  ApiClientOptions apiClientOptions,
                  ApiServiceProvider apiServiceProvider,
                  WebSocketServiceProvider webSocketServiceProvider,
                  UploadStorageProvider uploadStorageProvider,
                  StorageProvider storageProvider,
                  ConnectionLiveData connectionLiveData) {
        connected = false;
        this.apiKey = apiKey;
        this.apiClientOptions = apiClientOptions;
        subRegistry = new EventSubscriberRegistry();
        connectSubRegistry = new EventSubscriberRegistry<>();
        channelTypeConfigs = new HashMap<>();
        offlineStorage = false;
        this.apiServiceProvider = apiServiceProvider;
        this.webSocketServiceProvider = webSocketServiceProvider;
        this.uploadStorageProvider = uploadStorageProvider;
        this.storageProvider = storageProvider;
        this.state = new ClientState(this);
        this.webSocketDisconnectDelay = defaultWebSocketDelay;

        StreamChat.getLogger().logD(this,"instance created: " + apiKey);

        if (connectionLiveData != null) {
            connectionLiveData.observeForever(connectionModel -> {
                if (connectionModel.getIsConnected() && !connected) {
                    StreamChat.getLogger().logI(this,"fast track connection discovery: UP");
                    reconnectWebSocket();
                } else if (!connectionModel.getIsConnected() && connected) {
                    StreamChat.getLogger().logI(this,"fast track connection discovery: DOWN");
                    disconnectWebSocket();
                }
            });
        }
    }

    public Client(String apiKey, ApiClientOptions options) {
        this(apiKey,
                options,
                new StreamApiServiceProvider(options),
                new StreamWebSocketServiceProvider(options, apiKey),
                new StreamUploadStorageProvider(options),
                new StreamStorageProvider(),
                null);
    }

    public Client(String apiKey, ApiClientOptions options, ConnectionLiveData connectionLiveData) {
        this(apiKey,
                options,
                new StreamApiServiceProvider(options),
                new StreamWebSocketServiceProvider(options, apiKey),
                new StreamUploadStorageProvider(options),
                new StreamStorageProvider(),
                connectionLiveData);
    }

    public Storage getStorage() {
        if (storage == null) {
            storage = storageProvider.provideStorage(this, getContext(), offlineStorage);
        }
        return storage;
    }

    public String getApiKey() {
        return apiKey;
    }

    /**
     * Returns the current user set in client's state
     */
    public User getUser() {
        return state.getCurrentUser();
    }

    /**
     * Returns the current user set in client's state
     */
    public String getUserId() {
        User currentUser = state.getCurrentUser();
        if (currentUser == null) return null;
        return currentUser.getId();
    }

    public String getClientID() {
        return clientID;
    }

    /**
     * Set custom disconnection delay for websocket
     * @param delayInMilliseconds - time in milliseconds
     */
    public void setWebSocketDisconnectDelay(int delayInMilliseconds) {
        if (delayInMilliseconds < 0) {
            Log.e(TAG, "Value should be greater than 0");
            return;
        }
        this.webSocketDisconnectDelay = delayInMilliseconds;
    }

    public Map<String, Channel> getActiveChannelMap() {
        return activeChannelMap;
    }

    public List<Channel> getActiveChannels() {
        Collection<Channel> values = activeChannelMap.values();
        ArrayList<Channel> channels = new ArrayList<>(values);

        return channels;
    }

    public APIService getApiService() {
        return apiService;
    }

    public boolean isAnonymousConnection() {
        return anonymousConnection;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * The opposite of {@link #setUser(User, TokenProvider)} this closes the current WebSocket connection
     * and resets the client state as if setUser was never initialized
     * <p>
     * Calls to this method will return an error if a user was not set; if a user was set but
     * the connection is still pending (setUser is asynchronous) this method will also abort the pending
     * connection
     */
    public synchronized void disconnect() {
        if (state.getCurrentUser() == null) {
            StreamChat.getLogger().logW(this, "disconnect was called but setUser was not called yet");
        } else {
            StreamChat.getLogger().logD(this, "disconnecting");
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
        activeChannelMap.clear();
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
     * This can be done by adding callbacks via {@link #onSetUserCompleted(ClientConnectionCallback)}
     * <p>
     * Further calls to setUser are ignored; in order to change current user you first need to call
     * {@link #disconnect()}}
     *
     * @param user     the user to set as current
     * @param provider the Token Provider used to obtain the auth token for the user
     */
    public synchronized void setUser(@NotNull User user, @NotNull final TokenProvider provider) {
        anonymousConnection = false;

        if (getUser() != null) {
            StreamChat.getLogger().logW(this, "setUser was called but a user is already set; this is probably an integration mistake");
            return;
        }
        StreamChat.getLogger().logD(this,"setting user: " + user.getId());

        state.setCurrentUser(user);
        List<TokenProvider.TokenProviderListener> listeners = new ArrayList<>();

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
                    StreamChat.getLogger().logD(this,"Go get a new token");
                    fetchingToken = true;
                }

                provider.getToken(token -> {
                    cacheUserToken = token;
                    fetchingToken = false;
                    StreamChat.getLogger().logD(this,"We got another token " + token);
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
                StreamChat.getLogger().logD(this,"Current token is expired: " + cacheUserToken);
                cacheUserToken = null;
            }
        };
        connect(anonymousConnection);
    }

    /**
     * Generates a message id based on the user id + a random UUID.
     * We generate the message client side to make it easier to update the local storage/in-memory store of messages
     *
     * @return a string with the new message id
     */
    public String generateMessageID() {
        return getUserId() + "-" + randomUUID().toString();
    }

    public void setUser(User user, @NonNull String token, ClientConnectionCallback callback) {
        setUser(user, listener -> listener.onSuccess(token), callback);
    }

    public void setUser(User user, @NonNull String token) {
        setUser(user, listener -> listener.onSuccess(token));
    }

    public boolean fromCurrentUser(UserEntity entity) {
        String otherUserId = entity.getUserId();
        if (otherUserId == null) return false;
        if (getUser() == null) return false;
        return TextUtils.equals(getUserId(), otherUserId);
    }

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
            StreamChat.getLogger().logT(this, e);
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

    /**
     * Makes sure the callback is called when the user is ready
     * <p>
     * If the user is setup, it will run immediately; otherwise it will be added to a
     * waiting list and will be fired as soon as the user is ready (see {@link #setUser(User, TokenProvider)} for more)
     *
     * @param callback the callback to run when
     */
    public synchronized void onSetUserCompleted(ClientConnectionCallback callback) {
        if (connected) {
            callback.onSuccess(getUser());
        } else {
            connectSubRegistry.addSubscription(callback);
        }
    }

    private synchronized void connect(boolean anonymousConnection) {
        StreamChat.getLogger().logI(this,"client.connect was called");

        if (anonymousConnection) {
            try {
                webSocketService = webSocketServiceProvider.provideWebSocketService(getUser(), null, this, anonymousConnection);
            } catch (UnsupportedEncodingException e) {
                onError(e.getMessage(), ClientErrorCode.JSON_ENCODING);
            }
        } else {
            tokenProvider.getToken(token -> {
                try {
                    webSocketService = webSocketServiceProvider.provideWebSocketService(getUser(), token, this, anonymousConnection);
                } catch (UnsupportedEncodingException e) {
                    onError(e.getMessage(), ClientErrorCode.JSON_ENCODING);
                }
            });
        }

        apiService = apiServiceProvider.provideApiService(tokenProvider, anonymousConnection);
        uploadStorage = uploadStorageProvider.provideUploadStorage(tokenProvider, this);
        webSocketService.connect();
    }

    public Channel channel(String cid) {
        String[] parts = cid.split(":", 2);
        return channel(parts[0], parts[1], new HashMap<>());
    }

    public Channel channel(String type, String id) {
        return channel(type, id, new HashMap<>());
    }

    public Channel channel(String type, HashMap<String, Object> extraData, List<String> members) {
        return new Channel(this, type, extraData, members);
    }

    public Channel channel(String type, String id, HashMap<String, Object> extraData) {
        Channel channel = getChannelByCid(type, id);
        if (channel != null) {
            return channel;
        }
        return new Channel(this, type, id, extraData);
    }

    @Override
    public void connectionResolved(Event event) {
        clientID = event.getConnectionId();
        if (event.getMe() != null && !event.isAnonymous())
            state.setCurrentUser(event.getMe());

        // mark as connect, any new callbacks will automatically be executed
        connected = true;

        // call onSuccess for everyone that was waiting
        List<ClientConnectionCallback> subs = connectSubRegistry.getSubscribers();
        connectSubRegistry.clear();
        for (ClientConnectionCallback waiter : subs) {
            waiter.onSuccess(getUser());
        }

    }

    @Override
    public void onError(WsErrorMessage error) {
        onError(error.getError().getMessage(), error.getError().getCode());
    }

    private void onError(String errMsg, int errCode) {
        List<ClientConnectionCallback> subs = connectSubRegistry.getSubscribers();
        connectSubRegistry.clear();
        for (ClientConnectionCallback waiter : subs) {
            waiter.onError(errMsg, errCode);
        }
    }

    @Override
    public void onWSEvent(Event event) {
        builtinHandler.dispatchEvent(this, event);
        for (ChatEventHandler handler : subRegistry.getSubscribers()) {
            handler.dispatchEvent(this, event);
        }

        Channel channel = getChannelByCid(event.getCid());
        if (channel != null) {
            channel.handleChannelEvent(event);

            //If channel was deleted remove it from active channels
            if (event.getType().equals(EventType.CHANNEL_DELETED)) {
                activeChannelMap.remove(channel.getCid());
            }
        }
    }

    /**
     * the opposite of {@link #disconnectWebSocket()}
     */
    public void reconnectWebSocket() {
        if (getUser() == null) {
            StreamChat.getLogger().logW(this, "calling reconnectWebSocket before setUser is a no-op");
            return;
        }

        if (webSocketService != null) {
            StreamChat.getLogger().logW(this, "tried to reconnectWebSocket by a connection is still set");
            return;
        }
        connectionRecovered();

        connect(anonymousConnection);
    }

    @Override
    public void connectionRecovered() {
        List<String> cids = new ArrayList<>();
        for (Channel channel : getActiveChannels()) {
            cids.add(channel.getCid());
        }
        if (cids.size() > 0) {
            onSetUserCompleted(new ClientConnectionCallback() {
                @Override
                public void onSuccess(User user) {
                    QueryChannelsRequest query = new QueryChannelsRequest(and(in("cid", cids)), new QuerySort().desc("last_message_at"))
                            .withLimit(30)
                            .withMessageLimit(30);
                    queryChannels(query, new QueryChannelListCallback() {
                        @Override
                        public void onSuccess(QueryChannelsResponse response) {
                            connected = true;
                            onWSEvent(new Event(EventType.CONNECTION_RECOVERED.label));
                        }

                        @Override
                        public void onError(String errMsg, int errCode) {
                            // TODO: probably the best is to make sure the client goes back offline and online again
                        }
                    });
                }

                @Override
                public void onError(String errMsg, int errCode) {

                }
            });
        } else {
            onSetUserCompleted(new ClientConnectionCallback() {
                @Override
                public void onSuccess(User user) {
                    onWSEvent(new Event(EventType.CONNECTION_RECOVERED.label));
                }

                @Override
                public void onError(String errMsg, int errCode) {

                }
            });
        }
    }

    @Override
    public void tokenExpired() {
        tokenProvider.tokenExpired();
        disconnectWebSocket();
        reconnectWebSocket();
    }

    public synchronized void addChannelConfig(String channelType, Config config) {
        channelTypeConfigs.put(channelType, config);
    }

    public synchronized Config getChannelConfig(String channelType) {
        return channelTypeConfigs.get(channelType);
    }

    public synchronized void addToActiveChannels(Channel channel) {
        if (getChannelByCid(channel.getCid()) == null) {
            activeChannelMap.put(channel.getCid(), channel);
        }
    }

    // endregion

    private Channel getChannelByCid(String type, String id) {
        return getChannelByCid(type + ":" + id);
    }

    Channel getChannelByCid(String cid) {
        return activeChannelMap.get(cid);
    }

    // region Channel
    public void queryChannels(QueryChannelsRequest request, QueryChannelListCallback callback) {
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                String payload = GsonConverter.Gson().toJson(request);

                apiService.queryChannels(apiKey, getUserId(), clientID, payload).enqueue(new Callback<QueryChannelsResponse>() {
                    @Override
                    public void onResponse(Call<QueryChannelsResponse> call, Response<QueryChannelsResponse> response) {

                        for (ChannelState channelState : response.body().getChannelStates()) {
                            if (channelState.getLastMessage() != null)
                                channelState.getLastMessage().setSyncStatus(SYNCED);
                            Channel channel = channelState.getChannel();
                            addChannelConfig(channel.getType(), channel.getConfig());
                            channel.setClient(Client.this);
                            channel.setLastState(channelState);
                            if (getChannelByCid(channel.getCid()) != null) {
                                channel = getChannelByCid(channel.getCid());
                            } else {
                                addToActiveChannels(channel);
                            }
                            channel.mergeWithState(channelState);
                            if (request.isWatch()) {
                                channel.setInitialized(true);
                            }

                            // update the user references
                            state.updateUsersForChannel(channelState);
                        }

                        // store the results of the query
                        QueryChannelsQ query = request.query();

                        List<Channel> channels = response.body().getChannels();

                        getStorage().insertQueryWithChannels(query, channels);

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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                UpdateChannelRequest request = new UpdateChannelRequest(channel.getExtraData(), updateMessage);
                apiService.updateChannel(channel.getType(), channel.getId(), apiKey, clientID, request)
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                final ChannelQueryRequest queryRequest = request.withData(channel.getExtraData());
                Callback<ChannelState> requestCallback = new Callback<ChannelState>() {
                    @Override
                    public void onResponse(Call<ChannelState> call, Response<ChannelState> response) {
                        StreamChat.getLogger().logI(this,"channel query: incoming watchers " + response.body().getWatchers().size());
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

                        addChannelConfig(channel.getType(), channel.getConfig());

                        if (queryRequest.isWatch()) {
                            addToActiveChannels(channel);
                            channel.setInitialized(true);
                        }

                        // update the user references
                        getState().updateUsersForChannel(channel.getChannelState());

                        StreamChat.getLogger().logI(this,"channel query: merged watchers " + channel.getChannelState().getWatchers().size());
                        // offline storage

                        getStorage().insertMessagesForChannel(channel, response.body().getMessages());
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
                    apiService.queryChannel(channel.getType(), apiKey, getUserId(), clientID,
                            queryRequest).enqueue(requestCallback);
                } else {
                    apiService.queryChannel(channel.getType(), channel.getId(), apiKey, getUserId(),
                            clientID, queryRequest).enqueue(requestCallback);
                }
            }

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    /**
     * hides the channel from queryChannels for the user until a message is added TODO: track hidden state in Room
     *
     * @param channel  the channel needs to hide
     * @param callback the result callback
     */
    public void hideChannel(@NonNull Channel channel, HideChannelRequest request, @NotNull CompletableCallback callback) {
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                apiService.hideChannel(channel.getType(), channel.getId(), apiKey, clientID, request)
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        message.setUser(getUser());
        String str = GsonConverter.Gson().toJson(message);
        Map<String, Object> map = new HashMap<>();
        map.put("message", GsonConverter.Gson().fromJson(str, Map.class));

        apiService.sendMessage(channel.getType(), channel.getId(), apiKey, getUserId(), clientID, map).enqueue(new Callback<MessageResponse>() {
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
                getUserId(),
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

        apiService.getMessage(messageId, apiKey, getUserId(), clientID).enqueue(new Callback<MessageResponse>() {
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

        apiService.deleteMessage(messageId, apiKey, getUserId(), clientID).enqueue(new Callback<MessageResponse>() {
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

        Config channelConfig = getChannelConfig(channel.getType());
        if (channelConfig == null || !channelConfig.isReadEvents()) {
            callback.onError("Read events are disabled for this channel type", -1);
        } else {
            apiService.markRead(channel.getType(), channel.getId(), apiKey, getUserId(), clientID, readRequest).enqueue(new Callback<EventResponse>() {
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
    }

    /**
     * search messages by parameters
     *
     * @param request  request options include filter, query string and query options
     * @param callback the result callback
     */
    public void searchMessages(@NotNull SearchMessagesRequest request, @NotNull SearchMessagesCallback callback) {
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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

        apiService.markAllRead(apiKey, getUserId(), clientID).enqueue(new Callback<EventResponse>() {
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
            apiService.getReplies(parentId, apiKey, getUserId(), clientID, limit).enqueue(new Callback<GetRepliesResponse>() {
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
            apiService.getRepliesMore(parentId, apiKey, getUserId(), clientID, limit, idLt).enqueue(new Callback<GetRepliesResponse>() {
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


        apiService.sendReaction(reactionRequest.getReaction().getMessageId(), apiKey, getUserId(), clientID, reactionRequest).enqueue(new Callback<MessageResponse>() {
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

        apiService.deleteReaction(messageId, type, apiKey, getUserId(), clientID).enqueue(new Callback<MessageResponse>() {
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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

        apiService.sendEvent(channel.getType(), channel.getId(), apiKey, getUserId(), clientID, eventRequest).enqueue(new Callback<EventResponse>() {
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                String requestString = GsonConverter.Gson().toJson(request);
                apiService.queryUsers(apiKey, clientID, requestString)
                        .enqueue(new Callback<QueryUserListResponse>() {
                            @Override
                            public void onResponse(Call<QueryUserListResponse> call, Response<QueryUserListResponse> response) {
                                state.updateUsers(response.body().getUsers());
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });

    }

    /**
     * Setup an anonymous session
     */
    public void setAnonymousUser() {
        if (getUser() != null) {
            StreamChat.getLogger().logW(this, "setAnonymousUser was called but a user is already set;");
            return;
        }

        anonymousConnection = true;
        apiService = apiServiceProvider.provideApiService(null, true);

        String uuid = randomUUID().toString();

        state.setCurrentUser(new User(uuid));

        connect(anonymousConnection);
    }

    /**
     * Setup a temporary guest user
     *
     * @param user Data about this user. IE {name: "john"}
     */
    public void setGuestUser(User user) {
        if (getUser() != null) {
            StreamChat.getLogger().logW(this, "setGuestUser was called but a user is already set;");
            return;
        }

        GuestUserRequest body = new GuestUserRequest(user.getId(), user.getName());

        apiService = apiServiceProvider.provideApiService(null, true);
        apiService.setGuestUser(apiKey, body).enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                if (response.body() != null) {
                    setUser(user, response.body().getToken());
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                StreamChat.getLogger().logE(this, "Problem with setting guest user: " + t.getMessage());
            }
        });
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
        body.put("user_id", getUserId());

        apiService.muteUser(apiKey, getUserId(), clientID, body).enqueue(new Callback<MuteUserResponse>() {
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
     * Unmutes another user, the reverse of {@link #muteUser(String, MuteUserCallback)}
     *
     * @param target_id the id of the user to un-mute
     * @param callback  the result callback
     */
    public void unmuteUser(@NonNull String target_id,
                           MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id", target_id);
        body.put("user_id", getUserId());

        apiService.unMuteUser(apiKey, getUserId(), clientID, body).enqueue(new Callback<MuteUserResponse>() {
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

        apiService.flag(apiKey, getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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

        apiService.unFlag(apiKey, getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
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
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
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

            @Override
            public void onError(String errMsg, int errCode) {
                callback.onError(errMsg, errCode);
            }
        });
    }

    // endregion
    public void sendAction(@NonNull String messageId,
                           @NonNull SendActionRequest request,
                           MessageCallback callback) {

        apiService.sendAction(messageId, apiKey, getUserId(), clientID, request).enqueue(new Callback<MessageResponse>() {
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
        onSetUserCompleted(
                new ClientConnectionCallback() {

                    @Override
                    public void onSuccess(User user) {
                        apiService.addDevices(apiKey, user.getId(), clientID, request).enqueue(new Callback<CompletableResponse>() {
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

                    @Override
                    public void onError(String errMsg, int errCode) {
                        callback.onError(errMsg, errCode);
                    }
                });
    }

    /**
     * Returns the devices associated with a current user
     */
    public void getDevices(GetDevicesCallback callback) {

        onSetUserCompleted(
                new ClientConnectionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        apiService.getDevices(apiKey, user.getId(), clientID).enqueue(new Callback<GetDevicesResponse>() {
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

                    @Override
                    public void onError(String errMsg, int errCode) {
                        callback.onError(errMsg, errCode);
                    }
                }
        );
    }

    /**
     * removeDevice - Removes the device with the given id. Clientside users can only delete their own devices
     */
    public void removeDevice(@NonNull String deviceId,
                             CompletableCallback callback) {
        onSetUserCompleted(
                new ClientConnectionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        apiService.deleteDevice(deviceId, apiKey, user.getId(), clientID).enqueue(new Callback<CompletableResponse>() {
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

                    @Override
                    public void onError(String errMsg, int errCode) {
                        callback.onError(errMsg, errCode);
                    }
                }
        );
    }

    public void cancelScheduleWebSocketDisconnect() {
        delayedDisconnectWebSocketHandler.removeCallbacksAndMessages(null);
    }

    public void disconnectWebSocketWithDelay() {
        delayedDisconnectWebSocketHandler.removeCallbacksAndMessages(null);
        delayedDisconnectWebSocketHandler.postDelayed(this::disconnectWebSocket, webSocketDisconnectDelay);
    }

    /**
     * closes the WebSocket connection and sends a connection.change event to all listeners
     */
    public synchronized void disconnectWebSocket() {
        StreamChat.getLogger().logI(this,"disconnecting websocket");
        if (webSocketService != null) {
            webSocketService.disconnect();
            webSocketService = null;
            clientID = null;
        }
        onWSEvent(new Event(false));
        connected = false;
    }

    public void flagMessage(@NonNull String targetMessageId,
                            FlagCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_message_id", targetMessageId);

        apiService.flag(apiKey, getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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

        apiService.unFlag(apiKey, getUserId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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

    public Boolean getOfflineStorage() {
        return offlineStorage;
    }

    public void setOfflineStorage(Boolean offlineStorage) {
        this.offlineStorage = offlineStorage;
    }

    public void enableOfflineStorage() {
        setOfflineStorage(true);
    }

    public void disableOfflineStorage() {
        setOfflineStorage(true);
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public BaseStorage getUploadStorage() {
        return uploadStorage;
    }
}