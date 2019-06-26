package com.getstream.sdk.chat.rest.core;

import android.util.Log;

import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.rest.BaseURL;
import com.getstream.sdk.chat.rest.WebSocketService;
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

    public WebSocketService wsConnection;

    public StreamChat(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setUser(User user, String userToken) {
        this.userToken = userToken;
        this.user = user;
        Global.streamChat = this;
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

    public void setupWebSocket() {
        Map<String, Object> jsonParameter = new HashMap<>();
        Map<String, Object> map = new HashMap<>();
        map.put("id", this.user.getId());
        map.put("name", this.user.getName());
        map.put("image", this.user.getImage());
        jsonParameter.put("user_details", map);
        jsonParameter.put("user_id", this.user.getId());
        jsonParameter.put("user_token", this.userToken);
        jsonParameter.put("server_determines_connection_id", true);
        JSONObject json = new JSONObject(jsonParameter);
        String wsURL = Global.baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + this.apiKey + "&authorization=" + this.userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);
        wsConnection.wsURL = wsURL;
        wsConnection.connect();
    }
}
