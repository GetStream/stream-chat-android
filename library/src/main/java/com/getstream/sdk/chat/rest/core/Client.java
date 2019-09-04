package com.getstream.sdk.chat.rest.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.getstream.sdk.chat.ConnectionLiveData;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.enums.Token;
import com.getstream.sdk.chat.interfaces.ClientConnectionCallback;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Config;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.TokenService;
import com.getstream.sdk.chat.model.Watcher;
import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.interfaces.DeviceCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.FlagUserCallback;
import com.getstream.sdk.chat.rest.interfaces.GetDevicesCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.interfaces.MuteUserCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelsRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.DevicesResponse;
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

import org.json.JSONObject;

import java.util.ArrayList;
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
    private String userToken;
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
                channel.handleChannelUpdated(channel, event);
            }

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
        this.options = options;
        this.offlineStorage = false;

        if (connectionLiveData != null) {
            connectionLiveData.observeForever(connectionModel -> {
                if (connectionModel.getIsConnected() && !connected) {
                    Log.i(TAG, "fast track connection discovery: UP");
                    if (WSConn != null) {
                        WSConn.reconnect();
                    }
                }
            });
        }
    }

    public Storage storage() {
        return Storage.getStorage(getContext(), this.offlineStorage);
    }

    public Client(String apiKey, ApiClientOptions options) {
        this(apiKey, new ApiClientOptions(), null);
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

    // Server-side Token
    public void setUser(User user, final TokenProvider provider) {
        try {
            this.user = user;
            provider.onResult((String token) -> {
                userToken = token;
                connect();
            });
        } catch (Exception e) {
            provider.onError(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    public User getTrackedUser(User user) {
        User knownUser = knownUsers.get(user.getId());
        return knownUser == null ? user : knownUser;
    }

    private void trackUser(User newUser){
        User user = knownUsers.get(newUser.getId());
        if (user == null) {
            knownUsers.put(newUser.getId(), newUser.shallowCopy());
        } else {
            user.shallowUpdate(newUser);
        }
    }

    private void trackUsersFromChannel(Channel channel){
        for (Watcher watcher: channel.getChannelState().getWatchers()) {
            trackUser(watcher.getUser());
        }
        for (Member member: channel.getChannelState().getMembers()) {
            trackUser(member.getUser());
        }
    }

    // Dev, Guest Token
    public void setUser(User user, Token token) throws Exception {
        this.user = user;
        switch (token) {
            case DEVELOPMENT:
                this.userToken = TokenService.devToken(user.getId());
                break;
            case HARDCODED:
                this.userToken = token.getToken();
                break;
            case GUEST:
                this.userToken = TokenService.createGuestToken(user.getId());
                break;
            default:
                break;
        }
        Log.d(TAG, "TOKEN: " + this.userToken);
        if (!TextUtils.isEmpty(this.userToken)) {
            connect();
        }
    }

    // endregion

    // Hardcoded Code token
    public void setUser(User user, @NonNull String token) {
        this.user = user;
        this.userToken = token;
        connect();
    }

    public boolean fromCurrentUser(UserEntity entity){
        String otherUserId = entity.getUserId();
        if (otherUserId == null) return false;
        if (user == null) return false;
        return TextUtils.equals(user.getId(), otherUserId);
    }

    public final synchronized int addEventHandler(ChatEventHandler handler) {
        int id = ++subscribersSeq;
        eventSubscribers.add(handler);
        eventSubscribersBy.put(id, handler);
        return id;
    }

    public final synchronized void removeEventHandler(Number handlerId) {
        ChatEventHandler handler = eventSubscribersBy.remove(handlerId);
        eventSubscribers.remove(handler);
    }

    public void onSetUserCompleted(ClientConnectionCallback callback){
        if (connected) {
            callback.onSuccess(user);
        } else {
            connectionWaiters.add(callback);
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
        jsonParameter.put("user_token", this.userToken);
        jsonParameter.put("server_determines_connection_id", true);
        return new JSONObject(jsonParameter);
    }

    private synchronized void connect() {
        BaseURL baseURL = new BaseURL(options.getLocation());

        JSONObject json = buildUserDetailJSON();
        String wsURL = baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + apiKey + "&authorization=" + userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);

        mService = RetrofitClient.getAuthorizedClient(baseURL.url(BaseURL.Scheme.https), userToken).create(APIService.class);
        WSConn = new WebSocketService(wsURL, user.getId(), this);
        WSConn.connect();
    }

    public Channel channel(String cid){
        String[] parts = cid.split(":", 2);
        return new Channel(this, parts[0], parts[1]);
    }

    public Channel channel(String type, String id){
        return new Channel(this, type, id);
    }

    public Channel channel(String type, String id, HashMap<String, Object> extraData){
        return new Channel(this, type, id, extraData);
    }

    @Override
    public void connectionResolved(Event event){
        clientID = event.getConnectionId();
        if (event.getMe() != null)
            user = event.getMe();

        connected = true;

        for (ClientConnectionCallback waiter: connectionWaiters) {
            waiter.onSuccess(user);
        }
        connectionWaiters.clear();
    }

    @Override
    public void onWSEvent(Event event) {
        builtinHandler.dispatchEvent(this, event);

        for (int i = eventSubscribers.size() - 1; i >= 0 ; i--) {
            ChatEventHandler handler = eventSubscribers.get(i);
            handler.dispatchEvent(this, event);
        }

        Channel channel = getChannelByCid(event.getCid());
        if (channel != null) {
            channel.handleChannelEvent(event);
        }
    }

    @Override
    public void connectionRecovered() {
        List<String> cids = new ArrayList<>();
        for (Channel channel: activeChannels) {
            cids.add(channel.getCid());
        }
        if (cids.size() > 0) {
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
        } else {
            onWSEvent(new Event(EventType.CONNECTION_RECOVERED.label));
        }
        connect();
    }

    public synchronized void addChannelConfig(String channelType, Config config) {
        channelTypeConfigs.put(channelType, config);
    }

    public synchronized Config getChannelConfig(String channelType) {
        return channelTypeConfigs.get(channelType);
    }

    public synchronized void addToActiveChannels(Channel channel){
        if (getChannelByCid(channel.getCid()) == null){
            activeChannels.add(channel);
        }
    }

    // endregion

    public Channel getChannelByCid(String cid) {
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

                // TODO: if offline read from offline storage...

                mService.queryChannels(apiKey, userID, clientID, payload).enqueue(new Callback<QueryChannelsResponse>() {
                    @Override
                    public void onResponse(Call<QueryChannelsResponse> call, Response<QueryChannelsResponse> response) {
                        for (ChannelState channelState: response.body().getChannelStates()) {
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
                        }
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<QueryChannelsResponse> call, Throwable t) {
                        callback.onError(t.getLocalizedMessage(), -1);
                    }
                });
            }

            @Override
            public void onError(String errMsg, int errCode) {

            }
        });
    }

    // region Message

    /**
     * deleteChannel - Delete the given channel
     *
     * @param channelId the Channel id needs to be specified
     * @return {object} Response that includes the channel
     */
    public void deleteChannel(@NonNull String channelType, @NonNull String channelId, QueryChannelCallback callback) {

        mService.deleteChannel(channelType, channelId, apiKey, user.getId(), clientID).enqueue(new Callback<ChannelState>() {
            @Override
            public void onResponse(Call<ChannelState> call, Response<ChannelState> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

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
                callback.onError(t.getLocalizedMessage(), -1);
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
                              @NonNull UpdateMessageRequest request,
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
                callback.onError(t.getLocalizedMessage(), -1);
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
                callback.onError(t.getLocalizedMessage(), -1);
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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * markRead - marks the channel read for current user, only works if the `read_events` setting is enabled
     */
    public void markRead(@NonNull Channel channel,
                         EventCallback callback) {
        markRead(channel, new MarkReadRequest(null), callback);
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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }
    // endregion

    // region Thread

    /**
     * markAllRead - marks all channels for this user as read
     *
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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }
    // endregion

    // region Reaction

    /**
     * getReplies - List the message replies for a parent message
     *
     * @param {type} parent_id The message parent id, ie the top of the thread
     * @param {type} options   Pagination params, ie {limit:10, idlte: 10}
     * @return {type} A channelResponse with a list of messages
     */
    public void getReplies(@NonNull String parentId,
                           String limit,
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
                    callback.onError(t.getLocalizedMessage(), -1);
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
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            });
        }

    }

    /**
     * sendReaction - Send a reaction about a message
     *
     * @param {string} messageID the message id
     * @param {object} reaction the reaction object for instance {type: 'love'}
     * @param {string} user_id the id of the user (used only for server side request) default null
     * @return {object} The Server Response
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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    // region Event

    /**
     * deleteReaction - Delete a reaction by user and type
     *
     * @param {string} messageID the id of the message from which te remove the reaction
     * @param {string} reactionType the type of reaction that should be removed
     * @param {string} user_id the id of the user (used only for server side request) default null
     * @return {object} The Server Response
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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    /**
     * sendEvent - Send an event on this channel
     *
     * @param {object} event for example {type: 'message.read'}
     * @return {object} The Server Response
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
                callback.onError(t.getLocalizedMessage(), -1);
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
                callback.onError(t.getLocalizedMessage(), -1);
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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // region User

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * queryUsers - Query users and watch user presence
     *
     * @param {object} filterConditions MongoDB style filter conditions
     * @param {object} sort             QuerySort options, for instance {last_active: -1}
     * @param {object} options          Option object, {presence: true}
     * @return {object} User Query Response
     */
    public void queryUsers(@NonNull JSONObject payload,
                           QueryUserListCallback callback) {

        mService.queryUsers(apiKey, user.getId(), clientID, payload).enqueue(new Callback<QueryUserListResponse>() {
            @Override
            public void onResponse(Call<QueryUserListResponse> call, Response<QueryUserListResponse> response) {
                for (User user : response.body().getUsers())
                    if (!fromCurrentUser(user)){
                        trackUser(user);
                    }
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<QueryUserListResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // region Device

    /**
     * addDevice - Adds a push device for a user.
     *
     * @param {string} id the device id
     * @param {string} push_provider the push provider (apn or firebase)
     * @param {string} [userID] the user id (defaults to current user)
     */
    public void addDevice(@NonNull String deviceId,
                          DeviceCallback callback) {

        AddDeviceRequest request = new AddDeviceRequest(deviceId);
        mService.addDevices(apiKey, user.getId(), clientID, request).enqueue(new Callback<DevicesResponse>() {
            @Override
            public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<DevicesResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * getDevices - Returns the devices associated with a current user
     *
     * @param {string} [userID] User ID. Only works on serversidex
     * @return {devices} Array of devices
     */
    public void getDevices(@NonNull Map<String, String> payload,
                           GetDevicesCallback callback) {

        mService.getDevices(apiKey, user.getId(), clientID, payload).enqueue(new Callback<GetDevicesResponse>() {
            @Override
            public void onResponse(Call<GetDevicesResponse> call, Response<GetDevicesResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<GetDevicesResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * removeDevice - Removes the device with the given id. Clientside users can only delete their own devices
     *
     * @param {string} id The device id
     * @param {string} [userID] The user id. Only specify this for serverside requests
     */
    public void removeDevice(@NonNull String deviceId,
                             DeviceCallback callback) {

        mService.deleteDevice(deviceId, apiKey, user.getId(), clientID).enqueue(new Callback<DevicesResponse>() {
            @Override
            public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<DevicesResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    public void disconnect() {
        Log.i(TAG, "disconnecting");
        WSConn.disconnect();
        connected = false;
        WSConn = null;
        clientID = null;
        onWSEvent(new Event(false));
    }

    public void reconnect() {
        if (user == null || userToken == null) {
            Log.e(TAG, "Client reconnect called before setUser, this is probably an integration mistake.");
            return;
        }
        connectionRecovered();
    }

    public void setAnonymousUser() {
    }

    /**
     * setGuestUser - Setup a temporary guest user
     *
     * @param {object} user Data about this user. IE {name: "john"}
     * @return {promise} Returns a promise that resolves when the connection is setup
     */
    public void setGuestUser() {
    }

    public void on() {
    }

    public void off() {

    }

    /** muteUser - mutes a user
     *
     * @param target_id
     * Only used with serverside auth
     * @returns Server response
     */
    public void muteUser(@NonNull String target_id,
                         MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id",target_id);
        body.put("user_id",user.getId());

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /** unmuteUser - unmutes a user
     *
     * @param target_id
     * Only used with serverside auth
     * @returns Server response
     */
    public void unmuteUser(@NonNull String target_id,
                           MuteUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_id",target_id);
        body.put("user_id",user.getId());

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void flagUser(@NonNull String targetUserId,
                         FlagUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_user_id",targetUserId);

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void unFlagUser(@NonNull String targetUserId,
                           FlagUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_user_id",targetUserId);

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void flagMessage(@NonNull String targetMessageId,
                            FlagUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_message_id",targetMessageId);

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void unFlagMessage(@NonNull String targetMessageId,
                              FlagUserCallback callback) {

        Map<String, String> body = new HashMap<>();
        body.put("target_message_id",targetMessageId);

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
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public Boolean getOfflineStorage() {
        return offlineStorage;
    }

    public void setOfflineStorage(Boolean offlineStorage) {
        this.offlineStorage = offlineStorage;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
