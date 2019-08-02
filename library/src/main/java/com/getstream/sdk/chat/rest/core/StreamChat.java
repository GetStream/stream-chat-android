package com.getstream.sdk.chat.rest.core;

import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.component.Component;
import com.getstream.sdk.chat.interfaces.ChannelEventHandler;
import com.getstream.sdk.chat.interfaces.ChannelListEventHandler;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.model.Member;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.model.TokenService;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.enums.Token;
import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.request.ChannelDetailRequest;
import com.getstream.sdk.chat.rest.response.ChannelResponse;
import com.getstream.sdk.chat.rest.response.ChannelUserRead;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.Global;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import okio.ByteString;

public class StreamChat implements WSResponseHandler {

    private static final String TAG = StreamChat.class.getSimpleName();
    // Main Params
    public String apiKey;
    public User user;
    public String userToken;
    public String connectionId;
    // Client params
    public List<ChannelResponse> channels = new ArrayList<>();
    public Map<String, List<Message>> ephemeralMessage = new HashMap<>(); // Key: Channeal ID, Value: ephemeralMessages
    private Channel activeChannel;
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

    public void setComponent(Component component) {
        Global.component = component;
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

    private void setUpWebSocket() {
        JSONObject json = this.buildUserDetailJSON();
        String wsURL = Global.baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + this.apiKey + "&authorization=" + this.userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);

        WebSocketService webSocketService = new WebSocketService();
        webSocketService.setWsURL(wsURL);
        webSocketService.setWSResponseHandler(this);
        webSocketService.connect();
    }
    // endregion

    // region Handle Event
    public void handleReceiveEvent(Event event) {

        if (event.getChannel() == null) return;

        switch (event.getType()) {
            case Event.notification_added_to_channel:
            case Event.channel_updated:
            case Event.channel_deleted:
                handleChannelEvent(event);
                break;
            case Event.message_new:
            case Event.message_updated:
            case Event.message_deleted:
            case Event.message_read:
                handleMessageEvent(event);
                break;
            case Event.reaction_new:
            case Event.reaction_deleted:
                handleReactionEvent(event);
                break;
            case Event.user_updated:
            case Event.user_presence_changed:
            case Event.user_watching_start:
            case Event.user_watching_stop:
                handleUserEvent(event);
                break;
            case Event.notification_invited:
            case Event.notification_invite_accepted:
                handleInvite(event);
                break;
            case Event.health_check:
            case Event.typing_start:
            case Event.typing_stop:
                break;
            default:
                break;
        }
    }

    // region Handle Channel Event
    public void handleChannelEvent(Event event) {
        switch (event.getType()) {
            case Event.notification_added_to_channel:
                queryChannel(event.getChannel());
                break;
            case Event.channel_deleted:
                deleteChannelResponse(event.getChannel());
                break;
            case Event.channel_updated:
                break;
            default:
                break;
        }
    }
    private void queryChannel(Channel channel) {
        channel.setType(ModelType.channel_messaging);
        Map<String, Object> messages = new HashMap<>();
        messages.put("limit", Constant.DEFAULT_LIMIT);
        Map<String, Object> data = new HashMap<>();
        ChannelDetailRequest request = new ChannelDetailRequest(messages, data, true, true);

        Global.mRestController.channelDetailWithID(channel.getId(), request, (ChannelResponse response) -> {
            if (!response.getMessages().isEmpty())
                Global.setStartDay(response.getMessages(), null);
            addChannelResponse(response);
        }, (String errMsg, int errCode) -> {
            Log.d(TAG, "Failed Connect Channel : " + errMsg);
        });
    }
    // endregion


    // region Handle Message Event
    public void handleMessageEvent(Event event) {
        ChannelResponse channelResponse = getChannelResponseById(event.getChannel().getId());
        if (channelResponse == null) return;
        Message message = event.getMessage();
        if (message == null) return;

        if (!message.getType().equals(ModelType.message_regular)) return;

        switch (event.getType()) {
            case Event.message_new:
                newMessage(channelResponse, message);
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
            case Event.message_read:
                readMessage(channelResponse, event);
            break;
            default:
                break;
        }
    }


    public void newMessage(ChannelResponse channelResponse, Message message) {
        Global.setStartDay(Arrays.asList(message), channelResponse.getLastMessage());
        channelResponse.getMessages().add(message);
        channels.remove(channelResponse);
        channels.add(0, channelResponse);
    }

    public void updateMessage(ChannelResponse channelResponse, Message message) {
        for (int i = 0; i < channelResponse.getMessages().size(); i++) {
            if (message.getId().equals(channelResponse.getMessages().get(i).getId())) {
                channelResponse.getMessages().set(i, message);
                break;
            }
        }
    }

    public void readMessage(ChannelResponse channelResponse, Event event) {
        channelResponse.setReadDateOfChannelLastMessage(event.getUser(), event.getCreated_at());
        channelResponse.getChannel().setLastMessageDate(event.getCreated_at());
    }
    // endregion

    // region Handle Reaction Event
    public void handleReactionEvent(Event event) {
        ChannelResponse channelResponse = getChannelResponseById(event.getChannel().getId());
        if (channelResponse == null) return;
        Message message = event.getMessage();
        if (message == null) return;

        if (!message.getType().equals(ModelType.message_regular)) return;
        updateMessage(channelResponse, message);
    }
    // endregion

    // region Handle User Event

    public void handleUserEvent(Event event){
        switch (event.getType()){
            case Event.user_updated:
                break;
            case Event.user_presence_changed:
                break;
            case Event.user_watching_start:
                break;
            case Event.user_watching_stop:
                break;
        }
    }

    // region Handle Invite
    public void handleInvite(Event event) {

    }
    // endregion




    @Override
    public void handleEventWSResponse(Event event) {
        if (TextUtils.isEmpty(connectionId)) {
            Global.noConnection = false;
            if (!TextUtils.isEmpty(event.getConnection_id())) {
                connectionId = event.getConnection_id();
                if (event.getMe() != null)
                    setUser(event.getMe());
            }
        }
        handleReceiveEvent(event);
    }

    @Override
    public void handleByteStringWSResponse(ByteString byteString) {

    }


    @Override
    public void onFailed(String errMsg, int errCode) {
        this.connectionId = null;
    }

    // region Channel
    public ChannelResponse getChannelResponseById(String id) {
        ChannelResponse response_ = null;
        for (ChannelResponse response : channels) {
            if (id.equals(response.getChannel().getId())) {
                response_ = response;
                break;
            }
        }
        return response_;
    }

    public ChannelResponse getPrivateChannel(User user) {
        String channelId1 = this.user.getId() + "-" + user.getId(); // Created by
        String channelId2 = user.getId() + "-" + this.user.getId(); // Invited by
        ChannelResponse channelResponse = null;
        for (ChannelResponse response : channels) {
            if (response.getChannel().getId().equals(channelId1) || response.getChannel().getId().equals(channelId2)) {
                channelResponse = response;
                break;
            }
        }
        return channelResponse;
    }

    public void addChannelResponse(ChannelResponse response) {
        boolean isContain = false;
        for (ChannelResponse response1 : channels) {
            if (response1.getChannel().getId().equals(response.getChannel().getId())) {
                channels.remove(response1);
                channels.add(response);
                isContain = true;
                Log.d(TAG, "Contain channel:" + response.getChannel().getId());
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

    public User getOpponentUser(ChannelResponse channelResponse) {
        if (channelResponse.getMembers() == null || channelResponse.getMembers().isEmpty())
            return null;
        if (channelResponse.getMembers().size() > 2) return null;
        User opponent = null;
        try {
            for (Member member : channelResponse.getMembers()) {
                if (!member.getUser().getId().equals(this.user.getId())) {
                    opponent = member.getUser();
                    break;
                }
            }
        } catch (Exception e) {
        }
        return opponent;
    }

    // endregion

    // region Message

    public void setEphemeralMessage(String channelId, Message message) {
        List<Message> messages = ephemeralMessage.get(channelId);
        if (messages == null) messages = new ArrayList<>();

        boolean isContain = false;
        for (Message message1 : messages) {
            if (message1.getId().equals(message.getId())) {
                messages.remove(message1);
                isContain = true;
                break;
            }
        }
        if (!isContain)
            messages.add(message);

        ephemeralMessage.put(channelId, messages);
    }

    public List<Message> getEphemeralMessages(String channelId, String parentId) {
        List<Message> ephemeralMessages = ephemeralMessage.get(channelId);
        if (ephemeralMessages == null) return null;

        List<Message> messages = new ArrayList<>();
        if (parentId == null) {
            for (Message message : ephemeralMessages) {
                if (message.getParent_id() == null)
                    messages.add(message);
            }
        } else {
            for (Message message : ephemeralMessages) {
                if (message.getParent_id() == null) continue;
                if (message.getParent_id().equals(parentId))
                    messages.add(message);
            }
        }
        return messages;
    }

    public void removeEphemeralMessage(String channelId, String messageId) {
        Log.d(TAG, "remove MessageId: " + messageId);
        List<Message> messages = ephemeralMessage.get(channelId);
        for (Message message : messages) {
            if (message.getId().equals(messageId)) {
                Log.d(TAG, "Message Removed!");
                messages.remove(message);
                break;
            }
        }
    }

    public String getMentionedText(Message message) {
        if (message == null) return null;
        String text = message.getText();
        if (message.getMentionedUsers() != null && !message.getMentionedUsers().isEmpty()) {
            for (User mentionedUser : message.getMentionedUsers()) {
                String userName = mentionedUser.getName();
                text = text.replace("@" + userName, "**" + "@" + userName + "**");
            }
        }
        return text;
    }

    // endregion

    public void sortUserReads(List<ChannelUserRead> reads) {
        Collections.sort(reads, (ChannelUserRead o1, ChannelUserRead o2) -> {
            return o1.getLast_read().compareTo(o2.getLast_read());
        });
    }

    public List<User> getReadUsers(ChannelResponse response, Message message) {
        if (response.getReads() == null || response.getReads().isEmpty()) return null;
        List<User> users = new ArrayList<>();

        for (int i = response.getReads().size() - 1; i >= 0; i--) {
            ChannelUserRead read = response.getReads().get(i);
            if (readMessage(read.getLast_read(), message.getCreated_at())) {
                if (!users.contains(read.getUser()) && !read.getUser().getId().equals(this.user.getId()))
                    users.add(read.getUser());
            }
        }
        return users;
    }
    public boolean readMessage(String lastReadMessageDate, String channelLastMesage) {
        if (lastReadMessageDate == null) return true;

        Global.messageDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date dateUserRead, dateChannelMessage;

        try {
            dateUserRead = Global.messageDateFormat.parse(lastReadMessageDate);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        try {
            dateChannelMessage = Global.messageDateFormat.parse(channelLastMesage);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (dateUserRead.equals(dateChannelMessage) || dateUserRead.after(dateChannelMessage)) {
            return true;
        }
        return false;
    }
}
