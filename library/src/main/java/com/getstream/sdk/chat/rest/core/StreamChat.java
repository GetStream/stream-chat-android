package com.getstream.sdk.chat.rest.core;

import android.util.Log;

import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.model.Token;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.channel.Channel;
import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.utils.Global;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class StreamChat {

    private final String TAG = StreamChat.class.getSimpleName();

    private String apiKey;
    private User user;
    private String userToken;
    private String clientID;

    private Channel channel;

    public StreamChat(String apiKey) {
        this.apiKey = apiKey;
        Global.streamChat = this;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // DevToken
    public void setUser(User user, final TokenProvider listener) {
        try {
            listener.onResult(this.userToken = Token.devToken(user.getId()), null);
        } catch (Exception e) {
            listener.onResult(null, e.getLocalizedMessage());
            e.printStackTrace();
        }
        Log.d(TAG, "TOKEN: " + this.userToken);
        this.user = user;
        setUpWebSocket();
    }

    // Hardcoded
    public void setUser(User user, String userToken) {
        this.userToken = userToken;
        Log.d(TAG, "TOKEN: " + this.userToken);
        this.user = user;
        setUpWebSocket();
    }

    public User getUser() {
        return user;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getUserToken() {
        return userToken;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    // region Customize Components
    public void setChannel(String channelType, String channelId, String channelName, String channelImage) {
        if (channelId == null) {
            this.channel = null;
            return;
        }

        Channel channel_ = new Channel();
        channel_.setType(channelType);
        channel_.setId(channelId);
        channel_.setName(channelName);
        channel_.setImageURL(channelImage);
        this.channel = channel_;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public Channel getChannel() {
        return channel;
    }

    // end region
    private void setUpWebSocket() {
        Map<String, Object> jsonParameter = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        if (this.getUser().getAdditionalFields() == null) {
            map.put("id", this.user.getId());
            map.put("name", this.user.getName());
            map.put("image", this.user.getImage());
            jsonParameter.put("user_details", map);
        } else {
            this.user.getAdditionalFields().put("id", this.user.getId());
            this.user.getAdditionalFields().put("name", this.user.getName());
            this.user.getAdditionalFields().put("image", this.user.getImage());
            jsonParameter.put("user_details", this.user.getAdditionalFields());
        }
        jsonParameter.put("user_id", this.user.getId());
        jsonParameter.put("user_token", this.userToken);
        jsonParameter.put("server_determines_connection_id", true);
        JSONObject json = new JSONObject(jsonParameter);
        String wsURL = Global.baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + this.apiKey + "&authorization=" + this.userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);
        Global.webSocketService.wsURL = wsURL;
        Global.webSocketService.connect();
    }
}
