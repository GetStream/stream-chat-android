package com.getstream.sdk.chat.rest.core;

import android.text.TextUtils;
import android.util.Log;

import com.getstream.sdk.chat.component.Component;
import com.getstream.sdk.chat.interfaces.TokenProvider;
import com.getstream.sdk.chat.model.TokenService;
import com.getstream.sdk.chat.model.User;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.enums.Token;
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

    public User getUser() {
        return user;
    }

    public void setComponent(Component component) {
        Global.component = component;
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

    private JSONObject buildUserDetailJSON(){
        HashMap<String, Object> jsonParameter = new HashMap<>();
        HashMap<String, Object> userDetails = new HashMap<>(this.getUser().getExtraData());

        userDetails.put("id", this.getUser().getId());
        userDetails.put("name", this.getUser().getName());
        userDetails.put("image", this.getUser().getImage());

        jsonParameter.put("user_details", userDetails);
        jsonParameter.put("user_id", this.user.getId());
        jsonParameter.put("user_token", this.userToken);
        jsonParameter.put("server_determines_connection_id", true);
        return new JSONObject(jsonParameter);
    }
    // end region
    private void setUpWebSocket() {
        JSONObject json = this.buildUserDetailJSON();
        String wsURL = Global.baseURL.url(BaseURL.Scheme.webSocket) + "connect?json=" + json + "&api_key="
                + this.apiKey + "&authorization=" + this.userToken + "&stream-auth-type=" + "jwt";
        Log.d(TAG, "WebSocket URL : " + wsURL);
        Global.webSocketService.setWsURL(wsURL);
        Global.webSocketService.connect();
    }
}
