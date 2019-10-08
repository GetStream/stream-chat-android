package com.getstream.sdk.chat.rest.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.ConnectionLiveData;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.MessageStatus;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Config;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.QueryChannelsQ;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.interfaces.ChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagCallback;
import com.getstream.sdk.chat.rest.interfaces.GetDevicesCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.MuteUserCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.BanUserRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateChannelRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.FlagResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.rest.response.MuteUserResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.storage.Storage;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.in;

public class Client implements WSResponseHandler {

    private static final String TAG = Client.class.getSimpleName();
    private String clientID;
    private HashMap<String, User> knownUsers = new HashMap<>();
    // Main Params
    private String apiKey;
    private Boolean offlineStorage;
    private User user;
    private CachedTokenProvider tokenProvider;
    private boolean fetchingToken;
    private String cacheUserToken;
    private Context context;
    // Client params
    private List<Channel> activeChannels = new ArrayList<>();
    private boolean connected;

    private List<ClientConnectionCallback> connectionWaiters;
    private APIService mService;
    private List<ChatEventHandler> eventSubscribers;
    private Map<Number, ChatEventHandler> eventSubscribersBy;
    private int subscribersSeq;
    private Map<String, Config> channelTypeConfigs;
    private WebSocketService WSConn;
    private ApiClientOptions options;
    // endregion
    private ChatEventHandler builtinHandler =

            new ChatEventHandler() {
                @Override
                public void onAnyEvent(Event event) {
                    if (event.getUser() != null) {
                        trackUser(event.getUser());
                    }
                    Channel channel = getChannelByCid(event.getCid());
                    if (channel != null) {
                        trackUsersFromChannel(channel);
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
                    storage().deleteChannel(channel);
                    activeChannels.remove(channel);
                }

                // TODO: what about user update events?

                @Override
                public void onConnectionChanged(Event event) {
                    if (!event.getOnline()) {
                        connected = false;
                    }
                }
            };

    public Client(String apiKey, ApiClientOptions options, ConnectionLiveData connectionLiveData) {
        connected = false;
        this.apiKey = apiKey;
        eventSubscribers = new ArrayList<>();
        eventSubscribersBy = new HashMap<>();
        connectionWaiters = new ArrayList<>();
        channelTypeConfigs = new HashMap<>();
        offlineStorage = false;
        this.options = options;

        if (connectionLiveData != null) {
            connectionLiveData.observeForever(connectionModel -> {
                if (connectionModel.getIsConnected() && !connected) {
                    Log.i(TAG, "fast track connection discovery: UP");
                    if (WSConn != null) {
                        reconnectWebSocket();
                    }
                } else if (!connectionModel.getIsConnected() && connected) {
                    Log.i(TAG, "fast track connection discovery: DOWN");
                    disconnectWebSocket();
                }
            });
        }
    }

    public Client(String apiKey, ApiClientOptions options) {
        this(apiKey, new ApiClientOptions(), null);
    }

    public synchronized List<ClientConnectionCallback> getConnectionWaiters() {
        return connectionWaiters;
    }

    public Storage storage() {
        return Storage.getStorage(getContext(), this.offlineStorage);
    }

    public String getApiKey() {
        return apiKey;
    }

    public User getUser() {
        return user;
    }

    public String getUserId() {
        return user.getId();
    }

    public String getClientID() {
        return clientID;
    }

    public List<Channel> getActiveChannels() {
        return activeChannels;
    }

    public APIService getApiService() {
        return mService;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * The opposite of {@link #setUser(User, TokenProvider)} this closes the current WebSocket connection
     * and resets the client state as if setUser was never initialized
     *
     * Calls to this method will return an error if a user was not set; if a user was set but
     * the connection is still pending (setUser is asynchronous) this method will also abort the pending
     * connection
     */
    public synchronized void disconnect() {
        if (user == null) {
            Log.w(TAG, "disconnect was called but setUser was not called yet");
        }

        disconnectWebSocket();

        // unset token facilities
        tokenProvider = null;
        fetchingToken = false;
        cacheUserToken = null;

        // clear local state
        user = null;
        activeChannels.clear();
    }

    /**
     * Sets the current user for chat
     *
     * 1. it sets the current user to the client
     * 2. it requests the token from the provided TokenProvider
     * 3. uses {@link #connect} to continue with the initialization process
     *
     * This method is required for most of Chat SDK functionality to work; since this is an async
     * function (a WebSocket connection must be established) code that depends on the initialization
     * of the user should be not be called directly but await for setUser to be completed
     *
     * This can be done by adding callbacks via {@link #onSetUserCompleted(ClientConnectionCallback)}
     *
     * Further calls to setUser are ignored; in order to change current user you first need to call
     * {@link #disconnect()}}
     *
     * @param user the user to set as current
     * @param provider the Token Provider used to obtain the auth token for the user
     */
    public synchronized void setUser(User user, final TokenProvider provider) {

        if (this.user != null) {
            Log.w(TAG, "setUser was called but a user is already set; this is probably an integration mistake");
            return;
        }

        this.user = user;
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
        connect();
    }

    public User getTrackedUser(User user) {
        User knownUser = knownUsers.get(user.getId());
        return knownUser == null ? user : knownUser;
    }

    private void trackUser(User newUser) {
        User user = knownUsers.get(newUser.getId());
        if (user == null) {
            knownUsers.put(newUser.getId(), newUser.shallowCopy());
        } else {
            user.shallowUpdate(newUser);
        }
    }

    private void trackUsersFromChannel(Channel channel) {
        for (Watcher watcher : channel.getChannelState().getWatchers()) {
            trackUser(watcher.getUser());
        }
        for (Member member : channel.getChannelState().getMembers()) {
            trackUser(member.getUser());
        }
    }

    public void setUser(User user, @NonNull String token) {
        setUser(user, listener -> listener.onSuccess(token));
    }

    public boolean fromCurrentUser(UserEntity entity) {
        String otherUserId = entity.getUserId();
        if (otherUserId == null) return false;
        if (user == null) return false;
        return TextUtils.equals(user.getId(), otherUserId);
    }

    /**
     * Event Delegation: Adds an event handler for client events received via WebSocket
     *
     * @param handler the event handler for client events
     * @return the identifier of the handler, you can use that to remove it, see: {@link #removeEventHandler(Number)}
     */
    public final synchronized int addEventHandler(ChatEventHandler handler) {
        int id = ++subscribersSeq;
        eventSubscribers.add(handler);
        eventSubscribersBy.put(id, handler);
        return id;
    }

    /**
     * Event Delegation: removes an event handler via its id
     *
     * Removing an handler that was not registered is a no-op
     *
     * @param handlerId the event handler for client events
     */
    public final synchronized void removeEventHandler(Number handlerId) {
        ChatEventHandler handler = eventSubscribersBy.remove(handlerId);
        eventSubscribers.remove(handler);
    }

    /**
     * Makes sure the callback is called when the user is ready
     *
     * If the user is setup, it will run immediately; otherwise it will be added to a
     * waiting list and will be fired as soon as the user is ready (see {@link #setUser(User, TokenProvider)} for more)
     *
     * @param callback the callback to run when
     */
    public synchronized void onSetUserCompleted(ClientConnectionCallback callback) {
        if (connected) {
            callback.onSuccess(user);
        } else {
            getConnectionWaiters().add(callback);
        }
    }

    private JSONObject buildUserDetailJSON() {
        HashMap<String, Object> jsonParameter = new HashMap<>();
        HashMap<String, Object> userDetails = new HashMap<>();

        if (user.getExtraData() != null) {
            userDetails = new HashMap<>(user.getExtraData());
        }

        userDetails.put("id", this.user.getId());
        userDetails.put("name", this.user.getName());
        userDetails.put("image", this.user.getImage());

        jsonParameter.put("user_details", userDetails);
        jsonParameter.put("user_id", this.user.getId());
        jsonParameter.put("server_determines_connection_id", true);
        return new JSONObject(jsonParameter);
    }

    private synchronized void connect() {
        Log.i(TAG, "client.connect was called");
        tokenProvider.getToken(userToken -> {
            JSONObject json = buildUserDetailJSON();
            String wsURL = options.getWssURL() + "connect?json=" + json + "&api_key="
                    + apiKey + "&authorization=" + userToken + "&stream-auth-type=" + "jwt";
            Log.d(TAG, "WebSocket URL : " + wsURL);

            mService = RetrofitClient.getAuthorizedClient(tokenProvider, options).create(APIService.class);
            WSConn = new WebSocketService(wsURL, user.getId(), this);
            WSConn.connect();
        });
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
    public synchronized void connectionResolved(Event event) {
        clientID = event.getConnectionId();
        if (event.getMe() != null)
            user = event.getMe();

        connected = true;

        for (ClientConnectionCallback waiter : getConnectionWaiters()) {
            waiter.onSuccess(user);
        }
        getConnectionWaiters().clear();
    }

    @Override
    public void onWSEvent(Event event) {
        builtinHandler.dispatchEvent(this, event);
        for (int i = eventSubscribers.size() - 1; i >= 0; i--) {
            ChatEventHandler handler = eventSubscribers.get(i);
            handler.dispatchEvent(this, event);
        }

        Channel channel = getChannelByCid(event.getCid());
        if (channel != null) {
            channel.handleChannelEvent(event);
        }
    }

    /**
     * the opposite of {@link #disconnectWebSocket()}
     */
    public void reconnectWebSocket() {
        if (user == null) {
            Log.w(TAG, "calling reconnectWebSocket before setUser is a no-op");
            return;
        }
        if (WSConn != null) {
            Log.w(TAG, "tried to reconnectWebSocket by a connection is still set");
            return;
        }
        connectionRecovered();
        connect();
    }

    @Override
    public void connectionRecovered() {
        List<String> cids = new ArrayList<>();
        for (Channel channel : activeChannels) {
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
            activeChannels.add(channel);
        }
    }

    // endregion

    private Channel getChannelByCid(String type, String id) {
        return getChannelByCid(type + ":" + id);
    }

    Channel getChannelByCid(String cid) {
        if (cid == null) {
            return null;
        }
        for (Channel channel : activeChannels) {
            if (cid.equals(channel.getCid())) {
                return channel;
            }
        }
        return null;
    }

    // region Channel
    public void queryChannels(QueryChannelsRequest request, QueryChannelListCallback callback) {
        Client m = this;
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                String userID = user.getId();
                String payload = GsonConverter.Gson().toJson(request);

                mService.queryChannels(apiKey, userID, clientID, payload).enqueue(new Callback<QueryChannelsResponse>() {
                    @Override
                    public void onResponse(Call<QueryChannelsResponse> call, Response<QueryChannelsResponse> response) {

                        for (ChannelState channelState : response.body().getChannelStates()) {
                            if (channelState.getLastMessage() != null)
                                channelState.getLastMessage().setStatus(MessageStatus.RECEIVED);
                            Channel channel = channelState.getChannel();
                            addChannelConfig(channel.getType(), channel.getConfig());
                            channel.setClient(m);
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
                        }

                        // store the results of the query
                        QueryChannelsQ query = request.query();

                        List<Channel> channels = response.body().getChannels();

                        storage().insertQueryWithChannels(query, channels);
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
     * @param options       the custom properties
     * @param updateMessage message allowing you to show a system message in the Channel that something changed
     * @param callback      the result callback
     */
    public void updateChannel(@NonNull Channel channel, @NotNull Map<String, Object> options,
                              @Nullable String updateMessage, @NotNull ChannelCallback callback) {
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                mService.updateChannel(channel.getType(), channel.getId(), apiKey, clientID,
                        new UpdateChannelRequest(options, updateMessage))
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
                mService.deleteChannel(channel.getType(), channel.getId(), apiKey, clientID)
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
     * hides the channel from queryChannels for the user until a message is added TODO: track hidden state in Room
     *
     * @param channel  the channel needs to hide
     * @param callback the result callback
     */
    public void hideChannel(@NonNull Channel channel, @NotNull CompletableCallback callback) {
        onSetUserCompleted(new ClientConnectionCallback() {
            @Override
            public void onSuccess(User user) {
                mService.hideChannel(channel.getType(), channel.getId(), apiKey, clientID, Collections.EMPTY_MAP)
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
                mService.showChannel(channel.getType(), channel.getId(), apiKey, clientID, Collections.EMPTY_MAP)
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


    // region Message

    /**
     * sendMessage - Send a message to this channel
     *
     * @param {object} message The Message object
     * @return {object} The Server Response
     */
    public void sendMessage(Channel channel,
                            @NonNull SendMessageRequest sendMessageRequest,
                            MessageCallback callback) {

        mService.sendMessage(channel.getType(), channel.getId(), apiKey, user.getId(), clientID, sendMessageRequest).enqueue(new Callback<MessageResponse>() {
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
     * updateMessage - Update the given message
     *
     * @param {object} message object, id needs to be specified
     * @return {object} Response that includes the message
     */
    public void updateMessage(@NonNull String messageId,
                              @NonNull SendMessageRequest request,
                              MessageCallback callback) {

        mService.updateMessage(messageId,
                apiKey,
                user.getId(),
                clientID,
                request).enqueue(new Callback<MessageResponse>() {

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

    public void getMessage(@NonNull String messageId,
                           MessageCallback callback) {

        mService.getMessage(messageId, apiKey, user.getId(), clientID).enqueue(new Callback<MessageResponse>() {
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
     * deleteMessage - Delete the given message
     *
     * @param {string} messageID the message id needs to be specified
     * @return {object} Response that includes the message
     */
    public void deleteMessage(@NonNull String messageId,
                              MessageCallback callback) {

        mService.deleteMessage(messageId, apiKey, user.getId(), clientID).enqueue(new Callback<MessageResponse>() {
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
     * markRead - Send the mark read event for this user, only works if the `read_events` setting is enabled
     */
    public void markRead(@NonNull Channel channel,
                         MarkReadRequest readRequest,
                         EventCallback callback) {

        Config channelConfig = getChannelConfig(channel.getType());
        if (channelConfig != null && !channelConfig.isReadEvents()) {
            callback.onError("Read events are disabled for this channel type", -1);
        }

        if (getChannelConfig(channel.getType()).isReadEvents())
            mService.markRead(channel.getType(), channel.getId(), apiKey, user.getId(), clientID, readRequest).enqueue(new Callback<EventResponse>() {
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

    // region Thread

    /**
     * markAllRead - marks all channels for this user as read
     */
    public void markAllRead(MarkReadRequest readRequest,
                            EventCallback callback) {

        mService.markAllRead(apiKey, user.getId(), clientID, readRequest).enqueue(new Callback<EventResponse>() {
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
     * getReplies - List the message replies for a parent message
     */
    public void getReplies(@NonNull String parentId,
                           int limit,
                           String firstId,
                           GetRepliesCallback callback) {

        if (TextUtils.isEmpty(firstId)) {
            mService.getReplies(parentId, apiKey, user.getId(), clientID, limit).enqueue(new Callback<GetRepliesResponse>() {
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
            mService.getRepliesMore(parentId, apiKey, user.getId(), clientID, limit, firstId).enqueue(new Callback<GetRepliesResponse>() {
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
     * sendReaction - Send a reaction about a message
     */
    public void sendReaction(@NonNull String messageId,
                             @NonNull ReactionRequest reactionRequest,
                             MessageCallback callback) {

        mService.sendReaction(messageId, apiKey, user.getId(), clientID, reactionRequest).enqueue(new Callback<MessageResponse>() {
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
     * deleteReaction - Delete a reaction by user and type
     */
    public void deleteReaction(@NonNull String messageId,
                               @NonNull String reactionType,
                               MessageCallback callback) {

        mService.deleteReaction(messageId, reactionType, apiKey, user.getId(), clientID).enqueue(new Callback<MessageResponse>() {
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

    /**
     * sendEvent - Send an event on this channel
     */
    public void sendEvent(@NonNull Channel channel,
                          @NonNull SendEventRequest eventRequest,
                          EventCallback callback) {

        mService.sendEvent(channel.getType(), channel.getId(), apiKey, user.getId(), clientID, eventRequest).enqueue(new Callback<EventResponse>() {
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

    // region File
    public void sendImage(@NonNull Channel channel,
                          MultipartBody.Part part,
                          SendFileCallback callback) {

        mService.sendImage(channel.getType(), channel.getId(), part, apiKey, user.getId(), clientID).enqueue(new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
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

    public void sendFile(@NonNull Channel channel,
                         MultipartBody.Part part,
                         SendFileCallback callback) {

        mService.sendFile(channel.getType(), channel.getId(), part, apiKey, user.getId(), clientID).enqueue(new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
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

    // region User

    /**
     * bans target user ID
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
                mService.banUser(apiKey, clientID,
                        new BanUserRequest(targetUserId, timeout, reason,
                                channel != null ? channel.getType() : null,
                                channel != null ? channel.getId() : null))
                        .enqueue(new Callback<CompletableResponse>() {
                            @Override
                            public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                                if (response.isSuccessful()) {
                                    callback.onSuccess(response.body());
                                }
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
     * removes the ban for target user ID
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
                mService.unBanUser(apiKey, clientID, targetUserId,
                        channel != null ? channel.getType() : null,
                        channel != null ? channel.getId() : null)
                        .enqueue(new Callback<CompletableResponse>() {
                            @Override
                            public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                                if (response.isSuccessful()) {
                                    callback.onSuccess(response.body());
                                }
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
     * queryUsers - Query users and watch user presence
     */
    public void queryUsers(@NonNull JSONObject payload,
                           QueryUserListCallback callback) {

        mService.queryUsers(apiKey, user.getId(), clientID, payload).enqueue(new Callback<QueryUserListResponse>() {
            @Override
            public void onResponse(Call<QueryUserListResponse> call, Response<QueryUserListResponse> response) {
                for (User user : response.body().getUsers())
                    if (!fromCurrentUser(user)) {
                        trackUser(user);
                    }
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
     * setAnonymousUser - Setup an anonymous session
     */
    public void setAnonymousUser() {
    }

    /**
     * setGuestUser - Setup a temporary guest user
     */
    public void setGuestUser(User user) {
    }

    /**
     * muteUser - mutes a user
     *
     * @param target_id Only used with serverside auth
     * @returns Server response
     */
    public void muteUser(@NonNull String target_id,
                         MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id", target_id);
        body.put("user_id", user.getId());

        mService.muteUser(apiKey, user.getId(), clientID, body).enqueue(new Callback<MuteUserResponse>() {
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
     * unmuteUser - unmutes a user
     *
     * @param target_id Only used with serverside auth
     * @returns Server response
     */
    public void unmuteUser(@NonNull String target_id,
                           MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id", target_id);
        body.put("user_id", user.getId());

        mService.unMuteUser(apiKey, user.getId(), clientID, body).enqueue(new Callback<MuteUserResponse>() {
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

        mService.flag(apiKey, user.getId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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

        mService.unFlag(apiKey, user.getId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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

    // endregion
    public void sendAction(@NonNull String messageId,
                           @NonNull SendActionRequest request,
                           MessageCallback callback) {

        mService.sendAction(messageId, apiKey, user.getId(), clientID, request).enqueue(new Callback<MessageResponse>() {
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
     * addDevice - Adds a push device for a user.
     */
    public void addDevice(@NonNull String deviceId,
                          CompletableCallback callback) {
        AddDeviceRequest request = new AddDeviceRequest(deviceId);
        onSetUserCompleted(
                new ClientConnectionCallback() {

                    @Override
                    public void onSuccess(User user) {
                        mService.addDevices(apiKey, user.getId(), clientID, request).enqueue(new Callback<CompletableResponse>() {
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
     * getDevices - Returns the devices associated with a current user
     */
    public void getDevices(@NonNull Map<String, String> payload,
                           GetDevicesCallback callback) {

        onSetUserCompleted(
                new ClientConnectionCallback() {
                    @Override
                    public void onSuccess(User user) {
                        mService.getDevices(apiKey, user.getId(), clientID, payload).enqueue(new Callback<GetDevicesResponse>() {
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
                        mService.deleteDevice(deviceId, apiKey, user.getId(), clientID).enqueue(new Callback<CompletableResponse>() {
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

    /**
     * closes the WebSocket connection and sends a connection.change event to all listeners
     */
    public synchronized void disconnectWebSocket() {
        Log.i(TAG, "disconnecting");
        if (WSConn != null) {
            WSConn.disconnect();
            WSConn = null;
            clientID = null;
        }
        onWSEvent(new Event(false));
        connected = false;
    }

    public void flagMessage(@NonNull String targetMessageId,
                            FlagCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_message_id", targetMessageId);

        mService.flag(apiKey, user.getId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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

        mService.unFlag(apiKey, user.getId(), clientID, body).enqueue(new Callback<FlagResponse>() {
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
}