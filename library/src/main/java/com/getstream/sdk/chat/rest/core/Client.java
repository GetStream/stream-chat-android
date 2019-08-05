package com.getstream.sdk.chat.rest.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.component.Component;
import com.getstream.sdk.chat.enums.EventType;
import com.getstream.sdk.chat.interfaces.ChannelListEventHandler;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.TokenService;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.enums.Token;
import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.interfaces.DeviceCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.GetDevicesCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.interfaces.MessageCallback;
import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.PaginationRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.DevicesResponse;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.EventResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;
import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;
import com.getstream.sdk.chat.rest.response.GetDevicesResponse;
import com.getstream.sdk.chat.rest.response.GetRepliesResponse;
import com.getstream.sdk.chat.rest.response.QueryUserListResponse;
import com.getstream.sdk.chat.rest.response.MessageResponse;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


//TODO: rename this into Client
//TODO: split this into two classes, Client and Channel
public class Client implements WSResponseHandler {

    private static final String TAG = Client.class.getSimpleName();

    // Main Params
    private String apiKey;
    public User user;
    private String userToken;
    public String connectionId;

    private Component component;
    // Client params
    public List<ChannelResponse> channels = new ArrayList<>();
    public List<User> users = new ArrayList<>();
    public Map<String, List<Message>> ephemeralMessage = new HashMap<>(); // Key: Channel ID, Value: ephemeralMessages
    private Channel activeChannel;

    private APIService mService;

    private List<ChatEventHandler> eventSubscribers;
    private Map<Number, ChatEventHandler> eventSubscribersBy;
    private int subscribersSeq;

    private WebSocketService WSConn;

    // Interfaces
    private ChannelListEventHandler channelListEventHandler;

    public void setChannelListEventHandler(ChannelListEventHandler channelListEventHandler) {
        this.channelListEventHandler = channelListEventHandler;
    }

    public Client(String apiKey) {
        this.apiKey = apiKey;
        eventSubscribers = new ArrayList<>();
        eventSubscribersBy = new HashMap<>();
        Global.client = this;
    }

    public Component getComponent() {
        if (this.component == null) {
            this.component = new Component();
            Global.component = this.component;
        }
        return component;
    }

    public void setComponent(Component component) {
        this.component = component;
        Global.component = component;
    }

    // region Setup Channel
    public void setActiveChannel(Channel activeChannel) {
        this.activeChannel = activeChannel;
    }

    public Channel getActiveChannel() {
        return activeChannel;
    }
    // endregion

    // region Setup User
    public void setUser(User user) {
        this.user = user;
    }

    // Server-side Token
    public void setUser(User user, final TokenProvider provider) {
        try {
            this.user = user;
            provider.onResult((String token) -> {
                userToken = token;
                setUpWebSocket();
            });
        } catch (Exception e) {
            provider.onError(e.getLocalizedMessage());
            e.printStackTrace();
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
            setUpWebSocket();
        }
    }

    // Hardcoded Code token
    public void setUser(User user, String token) throws Exception {
        if (TextUtils.isEmpty(token)) {
            throw new Exception("Token must be non-null");
        }
        this.user = user;
        this.userToken = token;
        if (!TextUtils.isEmpty(this.userToken)) {
            setUpWebSocket();
        }
    }

    // endregion

    public final int addEventHandler(ChatEventHandler handler) {
        int id = ++subscribersSeq;
        eventSubscribers.add(handler);
        eventSubscribersBy.put(id, handler);
        return id;
    }

    public final void removeEventHandler(Number handlerId) {
        ChatEventHandler handler = eventSubscribersBy.remove(handlerId);
        eventSubscribers.remove(handler);
    }

    private void publishEvents(Event event){
        for (ChatEventHandler handler: eventSubscribers) {
            handler.dispatchEvent(event);
        }
    }

    // region Websocket Setup
    private JSONObject buildUserDetailJSON() {
        HashMap<String, Object> jsonParameter = new HashMap<>();
        HashMap<String, Object> userDetails = new HashMap<>(user.getExtraData());

        userDetails.put("id", this.user.getId());
        userDetails.put("name", this.user.getName());
        userDetails.put("image", this.user.getImage());

        jsonParameter.put("user_details", userDetails);
        jsonParameter.put("user_id", this.user.getId());
        jsonParameter.put("user_token", this.userToken);
        jsonParameter.put("server_determines_connection_id", true);
        return new JSONObject(jsonParameter);
    }

    // TODO: kill Global baseURL
    // TODO: protect this from multiple calls
    private void setUpWebSocket() {
        JSONObject json = buildUserDetailJSON();
        String wsURL = Global.baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + apiKey + "&authorization=" + userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);


        WSConn = new WebSocketService(wsURL, "TODO", user.getId(), this);
        WSConn.connect();

        mService = RetrofitClient.getAuthorizedClient(userToken).create(APIService.class);
    }
    // endregion


    @Override
    public void handleWSConnectReply(Event event){
        Global.noConnection = false;
        connectionId = event.getConnection_id();
        if (event.getMe() != null)
            setUser(event.getMe());
        if (channelListEventHandler != null)
            channelListEventHandler.handleConnection();
    }

    @Override
    public void handleWSEvent(Event event) {
        handleReceiveEvent(event); //legacy code
        publishEvents(event);
    }

    @Override
    public void handleWSRecover(){
        //TODO: do the reconnection
    }

//    @Override
//    public void onFailed(String errMsg, int errCode) {
//        this.connectionId = null;
//
//        if (channelListEventHandler != null)
//            channelListEventHandler.onConnectionFailed(errMsg, errCode);
//
//        Event wentOffline = new Event(false);
//        publishEvents(wentOffline);
//    }

    public void handleReceiveEvent(Event event) {
        String channelId = null;
        try {
            String[] array = event.getCid().split(":");
            channelId = array[1];
        } catch (Exception e) {
        }

        if (channelId == null) return;

        switch (event.getType()) {
            case NOTIFICATION_ADDED_TO_CHANNEL:
            case CHANNEL_UPDATED:
            case CHANNEL_DELETED:
                handleChannelEvent(event, channelId);
                break;
            case MESSAGE_NEW:
            case MESSAGE_UPDATED:
            case MESSAGE_DELETED:
                handleMessageEvent(event, channelId);
                break;
            case MESSAGE_READ:
                readMessageEvent(event, channelId);
                break;
            case REACTION_NEW:
            case REACTION_DELETED:
                handleReactionEvent(event, channelId);
                break;
            default:
                break;
        }
    }

    // region Handle Channel Event

    public void handleChannelEvent(Event event, String channelId) {
        switch (event.getType()) {
            case NOTIFICATION_ADDED_TO_CHANNEL:
                if (Global.getChannelResponseById(channelId) == null) {
                    queryChannel(event.getChannel(), new QueryChannelCallback() {
                        @Override
                        public void onSuccess(ChannelResponse response) {
                            if (channelListEventHandler != null)
                                channelListEventHandler.updateChannels();
                        }

                        @Override
                        public void onError(String errMsg, int errCode) {
                        }
                    });
                }
                break;
            case CHANNEL_DELETED:
                deleteChannelResponse(event.getChannel());
                break;
            default:
                break;
        }
        if (channelListEventHandler != null)
            channelListEventHandler.updateChannels();
    }

    public void queryChannel(Channel channel, QueryChannelCallback callback) {
        channel.setType(ModelType.channel_messaging);
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();

        // Additional Field
        if (channel.getExtraData() != null) {
            Set<String> keys = channel.getExtraData().keySet();
            for (String key : keys) {
                Object value = channel.getExtraData().get(key);
                if (value != null)
                    data.put(key, value);
            }
        }

        QueryChannelRequest request = new QueryChannelRequest(messages, data, true, true);

        mService.queryChannel(channel.getId(), apiKey, user.getId(), connectionId, request).enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                if (response.isSuccessful()) {
                    ChannelResponse channelResponse = response.body();
                    checkEphemeralMessages(channelResponse);
                    if (!channelResponse.getMessages().isEmpty())
                        Global.setStartDay(channelResponse.getMessages(), null);

                    addChannelResponse(channelResponse);
                    callback.onSuccess(channelResponse);
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<ChannelResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void addChannelResponse(ChannelResponse response) {
        boolean isContain = false;
        for (ChannelResponse response1 : channels) {
            if (response1.getChannel().getId().equals(response.getChannel().getId())) {
                channels.remove(response1);
                channels.add(response);
                isContain = true;
                break;
            }
        }
        if (!isContain)
            channels.add(response);
    }

    public void deleteChannelResponse(Channel channel) {
        for (ChannelResponse response1 : channels) {
            if (response1.getChannel().getId().equals(channel.getId())) {
                channels.remove(response1);
                break;
            }
        }
    }

    public ChannelResponse getChannelResponseById(String id) {
        return Global.getChannelResponseById(id);
    }

    // endregion


    // region Handle Message Event
    public void handleMessageEvent(Event event, String channelId) {
        ChannelResponse channelResponse = Global.getChannelResponseById(channelId);
        if (channelResponse == null) return;
        Message message = event.getMessage();
        if (message == null) return;

        switch (event.getType()) {
            case MESSAGE_NEW:
                newMessageEvent(channelResponse, message);
                break;
            case MESSAGE_UPDATED:
            case MESSAGE_DELETED:
                for (int i = 0; i < channelResponse.getMessages().size(); i++) {
                    if (message.getId().equals(channelResponse.getMessages().get(i).getId())) {
                        // Deleted Message
                        if (event.getType().equals(EventType.MESSAGE_DELETED))
                            message.setText(Constant.MESSAGE_DELETED);

                        channelResponse.getMessages().set(i, message);
                        break;
                    }
                }
                break;
            default:
                break;
        }
        if (channelListEventHandler != null)
            channelListEventHandler.updateChannels();
    }


    public void newMessageEvent(ChannelResponse channelResponse, Message message) {
        Global.setStartDay(Arrays.asList(message), channelResponse.getLastMessage());
        channelResponse.getMessages().add(message);
        channels.remove(channelResponse);
        channels.add(0, channelResponse);
    }

    public void updateMessageEvent(ChannelResponse channelResponse, Message message) {
        for (int i = 0; i < channelResponse.getMessages().size(); i++) {
            if (message.getId().equals(channelResponse.getMessages().get(i).getId())) {
                channelResponse.getMessages().set(i, message);
                break;
            }
        }
    }

    public void readMessageEvent(Event event, String channelId) {
        ChannelResponse channelResponse = Global.getChannelResponseById(channelId);
        if (channelResponse == null) return;

        channelResponse.setReadDateOfChannelLastMessage(event.getUser(), event.getCreated_at());
        channelResponse.getChannel().setLastMessageDate(event.getCreated_at());
        if (channelListEventHandler != null)
            channelListEventHandler.updateChannels();
    }
    // endregion

    // region Handle Reaction Event
    public void handleReactionEvent(Event event, String channelId) {
        ChannelResponse channelResponse = Global.getChannelResponseById(channelId);
        if (channelResponse == null) return;
        Message message = event.getMessage();
        if (message == null) return;

        updateMessageEvent(channelResponse, message);
    }
    // endregion

    // region Channel
    public void queryChannels(JSONObject payload, QueryChannelListCallback callback) {
        mService.queryChannels(apiKey, user.getId(), connectionId, payload).enqueue(new Callback<QueryChannelsResponse>() {
            @Override
            public void onResponse(Call<QueryChannelsResponse> call, Response<QueryChannelsResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getChannels() == null || response.body().getChannels().isEmpty())
                        callback.onError("There is no any active Channel(s)!", -1);
                    else {
                        if (channels == null) {
                            channels = new ArrayList<>();
                        }
                        for (int i = 0; i < response.body().getChannels().size(); i++) {
                            ChannelResponse channelResponse = response.body().getChannels().get(i);
                            checkEphemeralMessages(channelResponse);
                            channels.add(channelResponse);
                        }
                        callback.onSuccess(response.body());
                    }
                } else {
                    callback.onError("There is no any active Channel(s)!", -1);
                }
            }

            @Override
            public void onFailure(Call<QueryChannelsResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    private void checkEphemeralMessages(ChannelResponse response) {
        if (response == null) return;
        List<Message> ephemeralMainMessages = Global.getEphemeralMessages(response.getChannel().getId(), null);
        if (ephemeralMainMessages != null && !ephemeralMainMessages.isEmpty()) {
            for (int i = 0; i < ephemeralMainMessages.size(); i++) {
                Message message = ephemeralMainMessages.get(i);
                if (response.getMessages().contains(message)) continue;
                response.getMessages().add(message);
            }
        }
    }

    /**
     * deleteChannel - Delete the given channel
     *
     * @param channelId the Channel id needs to be specified
     * @return {object} Response that includes the channel
     */
    public void deleteChannel(@NonNull String channelId, QueryChannelCallback callback) {

        mService.deleteChannel(channelId, apiKey, user.getId(), connectionId).enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void pagination(@NonNull String channelId, @NonNull PaginationRequest request, QueryChannelCallback callback) {
        mService.pagination(channelId, apiKey, user.getId(), connectionId, request).enqueue(new Callback<ChannelResponse>() {
            @Override
            public void onResponse(Call<ChannelResponse> call, Response<ChannelResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<ChannelResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion


    // region Message

    /**
     * sendMessage - Send a message to this channel
     *
     * @param {object} message The Message object
     * @return {object} The Server Response
     */
    public void sendMessage(@NonNull String channelId,
                            @NonNull SendMessageRequest sendMessageRequest,
                            MessageCallback callback) {

        mService.sendMessage(channelId, apiKey, user.getId(), connectionId, sendMessageRequest).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
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
                connectionId,
                request).enqueue(new Callback<MessageResponse>() {

            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void getMessage(@NonNull String messageId,
                           MessageCallback callback) {

        mService.getMessage(messageId, apiKey, user.getId(), connectionId).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
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

        mService.deleteMessage(messageId, apiKey, user.getId(), connectionId).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * markRead - Send the mark read event for this user, only works if the `read_events` setting is enabled
     *
     * @return {Promise} Description
     */
    public void markRead(@NonNull String channelId,
                         MarkReadRequest readRequest,
                         EventCallback callback) {

        mService.markRead(channelId, apiKey, user.getId(), connectionId, readRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    /**
     * markAllRead - marks all channels for this user as read
     *
     * @return {Promise} Description
     */
    public void markAllRead(MarkReadRequest readRequest,
                            EventCallback callback) {

        mService.markAllRead(apiKey, user.getId(), connectionId, readRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
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
            mService.getReplies(parentId, apiKey, user.getId(), connectionId, limit).enqueue(new Callback<GetRepliesResponse>() {
                @Override
                public void onResponse(Call<GetRepliesResponse> call, Response<GetRepliesResponse> response) {
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError(response.message(), response.code());
                    }
                }

                @Override
                public void onFailure(Call<GetRepliesResponse> call, Throwable t) {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            });
        } else {
            mService.getRepliesMore(parentId, apiKey, user.getId(), connectionId, limit, firstId).enqueue(new Callback<GetRepliesResponse>() {
                @Override
                public void onResponse(Call<GetRepliesResponse> call, Response<GetRepliesResponse> response) {
                    if (response.isSuccessful()) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError(response.message(), response.code());
                    }
                }

                @Override
                public void onFailure(Call<GetRepliesResponse> call, Throwable t) {
                    callback.onError(t.getLocalizedMessage(), -1);
                }
            });
        }

    }
    // endregion

    // region Reaction

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

        mService.sendReaction(messageId, apiKey, user.getId(), connectionId, reactionRequest).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<MessageResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

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

        mService.deleteReaction(messageId, reactionType, apiKey, user.getId(), connectionId).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
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
     * sendEvent - Send an event on this channel
     *
     * @param {object} event for example {type: 'message.read'}
     * @return {object} The Server Response
     */
    public void sendEvent(@NonNull String channelId,
                          @NonNull SendEventRequest eventRequest,
                          EventCallback callback) {

        mService.sendEvent(channelId, apiKey, user.getId(), connectionId, eventRequest).enqueue(new Callback<EventResponse>() {
            @Override
            public void onResponse(Call<EventResponse> call, Response<EventResponse> response) {

                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<EventResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    // region File
    public void sendImage(@NonNull String channelId,
                          MultipartBody.Part part,
                          SendFileCallback callback) {

        mService.sendImage(channelId, part, apiKey, user.getId(), connectionId).enqueue(new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Send File:" + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void sendFile(@NonNull String channelId,
                         MultipartBody.Part part,
                         SendFileCallback callback) {

        mService.sendFile(channelId, part, apiKey, user.getId(), connectionId).enqueue(new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Send File:" + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion
    public void sendAction(@NonNull String messageId,
                           @NonNull SendActionRequest request,
                           MessageCallback callback) {

        mService.sendAction(messageId, apiKey, user.getId(), connectionId, request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(Call<MessageResponse> call, Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Send File:" + response.body().toString());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // region User

    /**
     * queryUsers - Query users and watch user presence
     *
     * @param {object} filterConditions MongoDB style filter conditions
     * @param {object} sort             Sort options, for instance {last_active: -1}
     * @param {object} options          Option object, {presence: true}
     * @return {object} User Query Response
     */
    public void queryUsers(@NonNull JSONObject payload,
                           QueryUserListCallback callback) {

        mService.queryUsers(apiKey, user.getId(), connectionId, payload).enqueue(new Callback<QueryUserListResponse>() {
            @Override
            public void onResponse(Call<QueryUserListResponse> call, Response<QueryUserListResponse> response) {
                if (response.isSuccessful()) {
                    for (int i = 0; i < response.body().getUsers().size(); i++)
                        if (!response.body().getUsers().get(i).isMe())
                            users.add(response.body().getUsers().get(i));

                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<QueryUserListResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

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
        mService.addDevices(apiKey, user.getId(), connectionId, request).enqueue(new Callback<DevicesResponse>() {
            @Override
            public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> response) {
                if (callback != null)
                    callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<DevicesResponse> call, Throwable t) {
                if (callback != null)
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

        mService.getDevices(apiKey, user.getId(), connectionId, payload).enqueue(new Callback<GetDevicesResponse>() {
            @Override
            public void onResponse(Call<GetDevicesResponse> call, Response<GetDevicesResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(response.message(), response.code());
                }
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

        mService.deleteDevice(deviceId, apiKey, user.getId(), connectionId).enqueue(new Callback<DevicesResponse>() {
            @Override
            public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {

                    callback.onError(response.message(), response.code());
                }
            }

            @Override
            public void onFailure(Call<DevicesResponse> call, Throwable t) {
                callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    // endregion

    public void disconnect() {
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


    public void muteUser() {
    }

    public void unmuteUser() {
    }

    public void flagMessage() {
    }

    public void unflagMessage() {
    }
}
