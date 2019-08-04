package com.getstream.sdk.chat.rest.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.component.Component;
import com.getstream.sdk.chat.interfaces.ChannelEventHandler;
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
import com.getstream.sdk.chat.rest.interfaces.AddDeviceCallback;
import com.getstream.sdk.chat.rest.interfaces.EventCallback;
import com.getstream.sdk.chat.rest.interfaces.GetDevicesCallback;
import com.getstream.sdk.chat.rest.interfaces.GetRepliesCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryUserListCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelCallback;
import com.getstream.sdk.chat.rest.interfaces.QueryChannelListCallback;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.interfaces.SendMessageCallback;
import com.getstream.sdk.chat.rest.request.AddDeviceRequest;
import com.getstream.sdk.chat.rest.request.QueryChannelRequest;
import com.getstream.sdk.chat.rest.request.MarkReadRequest;
import com.getstream.sdk.chat.rest.request.PaginationRequest;
import com.getstream.sdk.chat.rest.request.ReactionRequest;
import com.getstream.sdk.chat.rest.request.SendActionRequest;
import com.getstream.sdk.chat.rest.request.SendEventRequest;
import com.getstream.sdk.chat.rest.request.SendMessageRequest;
import com.getstream.sdk.chat.rest.request.UpdateMessageRequest;
import com.getstream.sdk.chat.rest.response.AddDevicesResponse;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import okhttp3.MultipartBody;
import okio.ByteString;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StreamChat implements WSResponseHandler {

    private static final String TAG = StreamChat.class.getSimpleName();

    // Main Params
    public String apiKey;
    public User user;
    public String userToken;
    public String connectionId;
    private Component component;
    // Client params
    public List<ChannelResponse> channels = new ArrayList<>();
    public List<User> users = new ArrayList<>();
    public Map<String, List<Message>> ephemeralMessage = new HashMap<>(); // Key: Channeal ID, Value: ephemeralMessages
    private Channel activeChannel;

    public APIService mService;


    // Interfaces
    private ChannelListEventHandler channelListEventHandler;
    private ChannelEventHandler channelEventHandler;

    public void setChannelListEventHandler(ChannelListEventHandler channelListEventHandler) {
        this.channelListEventHandler = channelListEventHandler;
    }

    public void setChannelEventHandler(ChannelEventHandler channelEventHandler) {
        this.channelEventHandler = channelEventHandler;
    }

    public StreamChat(String apiKey) {
        this.apiKey = apiKey;
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

    // Harded Code token
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

    private void setUpWebSocket() {
        JSONObject json = this.buildUserDetailJSON();
        String wsURL = Global.baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + this.apiKey + "&authorization=" + this.userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);

        WebSocketService webSocketService = new WebSocketService();
        webSocketService.setWsURL(wsURL);
        webSocketService.setWSResponseHandler(this);
        webSocketService.connect();

        mService = RetrofitClient.getAuthorizedClient(userToken).create(APIService.class);
    }
    // endregion

    // region handle
    @Override
    public void handleEventWSResponse(Event event) {
        if (TextUtils.isEmpty(connectionId)) {
            Global.noConnection = false;
            if (!TextUtils.isEmpty(event.getConnection_id())) {
                connectionId = event.getConnection_id();
                if (event.getMe() != null)
                    setUser(event.getMe());
            }
            if (channelListEventHandler != null)
                channelListEventHandler.handleConnection();
            if (channelEventHandler != null)
                channelEventHandler.handleConnection();
        }
        handleReceiveEvent(event);
    }

    @Override
    public void handleByteStringWSResponse(ByteString byteString) {
    }


    @Override
    public void onFailed(String errMsg, int errCode) {
        this.connectionId = null;

        if (channelListEventHandler != null)
            channelListEventHandler.onConnectionFailed(errMsg, errCode);
        if (channelEventHandler != null)
            channelEventHandler.onConnectionFailed(errMsg, errCode);
    }

    public void handleReceiveEvent(Event event) {
        String channelId = null;
        try {
            String[] array = event.getCid().split(":");
            channelId = array[1];
        } catch (Exception e) {
        }

        if (channelId == null) return;

        switch (event.getType()) {
            case Event.notification_added_to_channel:
            case Event.channel_updated:
            case Event.channel_deleted:
                handleChannelEvent(event, channelId);
                break;
            case Event.message_new:
            case Event.message_updated:
            case Event.message_deleted:
                handleMessageEvent(event, channelId);
                break;
            case Event.message_read:
                readMessageEvent(event, channelId);
                break;
            case Event.reaction_new:
            case Event.reaction_deleted:
                handleReactionEvent(event, channelId);
                break;
            case Event.user_updated:
            case Event.user_presence_changed:
            case Event.user_watching_start:
            case Event.user_watching_stop:
                handleUserEvent(event, channelId);
                break;
            case Event.notification_invited:
            case Event.notification_invite_accepted:
                handleInvite(event);
                break;
            case Event.health_check:
                break;
            case Event.typing_start:
            case Event.typing_stop:
                if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
                    channelEventHandler.handleEventResponse(event);
                break;
            default:
                break;
        }
    }

    // region Handle Channel Event

    public void handleChannelEvent(Event event, String channelId) {
        switch (event.getType()) {
            case Event.notification_added_to_channel:
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
            case Event.channel_deleted:
                deleteChannelResponse(event.getChannel());
                if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
                    channelEventHandler.handleEventResponse(event);
                break;
            case Event.channel_updated:
                if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
                    channelEventHandler.handleEventResponse(event);
                break;
            default:
                break;
        }
        if (channelListEventHandler != null)
            channelListEventHandler.updateChannels();
    }

    public void queryChannel(Channel channel, final QueryChannelCallback callback) {
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

        if (!message.getType().equals(ModelType.message_regular)){
            if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
                channelEventHandler.handleEventResponse(event);
            return;
        }

        switch (event.getType()) {
            case Event.message_new:
                newMessageEvent(channelResponse, message);
                break;
            case Event.message_updated:
            case Event.message_deleted:
                for (int i = 0; i < channelResponse.getMessages().size(); i++) {
                    if (message.getId().equals(channelResponse.getMessages().get(i).getId())) {
                        // Deleted Message
                        if (event.getType().equals(Event.message_deleted))
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
        if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
            channelEventHandler.handleEventResponse(event);
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
        if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
            channelEventHandler.handleEventResponse(event);
    }
    // endregion

    // region Handle Reaction Event
    public void handleReactionEvent(Event event, String channelId) {
        ChannelResponse channelResponse = Global.getChannelResponseById(channelId);
        if (channelResponse == null) return;
        Message message = event.getMessage();
        if (message == null) return;

        updateMessageEvent(channelResponse, message);
        if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
            channelEventHandler.handleEventResponse(event);
    }
    // endregion

    // region Handle User Event

    public void handleUserEvent(Event event, String channelId) {
        switch (event.getType()) {
            case Event.user_updated:
                break;
            case Event.user_presence_changed:
                break;
            case Event.user_watching_start:
                break;
            case Event.user_watching_stop:
                break;
        }
        if (channelEventHandler != null && activeChannel != null && activeChannel.getId().equals(channelId))
            channelEventHandler.handleEventResponse(event);
    }

    // endregion

    // region Handle Invite
    public void handleInvite(Event event) {

    }
    // endregion

    // endregion

    // region Channel
    public void queryChannels(final QueryChannelListCallback callback) {
        mService.queryChannels(apiKey, user.getId(), connectionId, getPayload()).enqueue(new Callback<QueryChannelsResponse>() {
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

    private JSONObject getPayload() {
        Map<String, Object> payload = new HashMap<>();

        // Sort Option
        if (component.channel.getSortOptions() != null) {
            payload.put("sort", Collections.singletonList(component.channel.getSortOptions()));
        } else {
            Map<String, Object> sort = new HashMap<>();
            sort.put("field", "last_message_at");
            sort.put("direction", -1);
            payload.put("sort", Collections.singletonList(sort));
        }

        if (component.channel.getFilter() != null) {
            payload.put("filter_conditions", component.channel.getFilter().getData());
        } else {
            payload.put("filter_conditions", new HashMap<>());
        }

        payload.put("message_limit", Constant.CHANNEL_MESSAGE_LIMIT);
        if (channels.size() > 0)
            payload.put("offset", channels.size());
        payload.put("limit", Constant.CHANNEL_LIMIT);
        payload.put("presence", false);
        payload.put("state", true);
        payload.put("subscribe", true);
        payload.put("watch", true);
        return new JSONObject(payload);
    }

    /**
     * deleteChannel - Delete the given channel
     *
     * @param channelId the Channel id needs to be specified
     * @return {object} Response that includes the channel
     */
    public void deleteChannel(@NonNull String channelId, final QueryChannelCallback callback) {

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

    public void pagination(@NonNull String channelId, @NonNull PaginationRequest request, final QueryChannelCallback callback) {

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
    public void sendMessage(@NonNull String channelId, @NonNull SendMessageRequest sendMessageRequest, final SendMessageCallback callback) {

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
                              final SendMessageCallback callback) {

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

    /**
     * deleteMessage - Delete the given message
     *
     * @param {string} messageID the message id needs to be specified
     * @return {object} Response that includes the message
     */
    public void deleteMessage(@NonNull String messageId, final SendMessageCallback callback) {

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
    public void markRead(@NonNull String channelId, MarkReadRequest readRequest, final EventCallback callback) {

        mService.readMark(channelId, apiKey, user.getId(), connectionId, readRequest).enqueue(new Callback<EventResponse>() {
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
    public void getReplies(@NonNull String parentId, String limit, String firstId, final GetRepliesCallback callback) {
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
    public void sendReaction(@NonNull String messageId, @NonNull ReactionRequest reactionRequest, final SendMessageCallback callback) {

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
    public void deleteReaction(@NonNull String messageId, @NonNull String reactionType, final SendMessageCallback callback) {

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
    public void sendEvent(@NonNull String channelId, @NonNull SendEventRequest eventRequest, final EventCallback callback) {

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
    public void sendImage(@NonNull String channelId, MultipartBody.Part part, final SendFileCallback callback) {

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

    public void sendFile(@NonNull String channelId, MultipartBody.Part part, final SendFileCallback callback) {

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
    public void sendAction(@NonNull String messageId, SendActionRequest request, final SendMessageCallback callback) {

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
    public void queryUsers(final QueryUserListCallback callback) {
        mService.queryUsers(apiKey, user.getId(), connectionId, getUserQueryPayload()).enqueue(new Callback<QueryUserListResponse>() {
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

    private JSONObject getUserQueryPayload() {
        Map<String, Object> payload = new HashMap<>();

        // Filter options
        if (Global.component.user.getFilter() != null) {
            payload.put("filter_conditions", Global.component.user.getFilter().getData());
        } else {
            payload.put("filter_conditions", new HashMap<>());
        }
        // Sort options
        if (Global.component.user.getSortOptions() != null) {
            payload.put("sort", Collections.singletonList(Global.component.user.getSortOptions()));
        } else {
            Map<String, Object> sort = new HashMap<>();
            sort.put("field", "last_active");
            sort.put("direction", -1);
            payload.put("sort", Collections.singletonList(sort));
        }

        if (users.size() > 0)
            payload.put("offset", users.size());
        payload.put("limit", Constant.USER_LIMIT);

        JSONObject json;
        json = new JSONObject(payload);
        Log.d(TAG, "Payload: " + json);
        return json;
    }
    // endregion

    // region Device

    public void addDevice(String deviceId, final AddDeviceCallback callback) {
        AddDeviceRequest request = new AddDeviceRequest(deviceId);
        mService.addDevices(apiKey, user.getId(), connectionId, request).enqueue(new Callback<AddDevicesResponse>() {
            @Override
            public void onResponse(Call<AddDevicesResponse> call, Response<AddDevicesResponse> response) {
                if (callback != null)
                    callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call<AddDevicesResponse> call, Throwable t) {
                if (callback != null)
                    callback.onError(t.getLocalizedMessage(), -1);
            }
        });
    }

    public void getDevices(@NonNull Map<String, String> payload, final GetDevicesCallback callback) {
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


    // endregion
}
