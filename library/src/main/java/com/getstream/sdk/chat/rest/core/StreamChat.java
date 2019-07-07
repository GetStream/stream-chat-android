package com.getstream.sdk.chat.rest.core;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

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
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setUser(User user, String userToken) {
        this.userToken = userToken;
        this.user = user;
        Global.streamChat = this;
        setUpWebSocket();
    }

    public String createUserToken(@NonNull String userId) throws Exception{
        if (TextUtils.isEmpty(userId))
            throw new IllegalArgumentException("User ID must be non-null");

        String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9"; //  //{"alg": "HS256", "typ": "JWT"}
        JSONObject payloadJson = new JSONObject();

        payloadJson.put("user_id", userId);
        String payload = payloadJson.toString();
        String payloadBase64 = Base64.encodeToString(payload.getBytes("UTF-8"), Base64.NO_WRAP);
        String devSignature = "devtoken";

        String[] a = new String[3];
        a[0] = header;
        a[1] = payloadBase64;
        a[2] = devSignature;
        return TextUtils.join(".", a);
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
