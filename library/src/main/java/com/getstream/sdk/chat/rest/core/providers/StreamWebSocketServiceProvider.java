package com.getstream.sdk.chat.rest.core.providers;

import android.util.Log;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.rest.StreamWebSocketService;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.Client;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class StreamWebSocketServiceProvider implements WebSocketServiceProvider {
    private static final String TAG = Client.class.getSimpleName();

    private ApiClientOptions apiClientOptions;
    private String apiKey;

    public StreamWebSocketServiceProvider(ApiClientOptions options, String apiKey) {
        this.apiClientOptions = options;
        this.apiKey = apiKey;
    }

    @Override
    public WebSocketService provideWebSocketService(User user, String userToken, WSResponseHandler listener) throws UnsupportedEncodingException {
        String wsUrl = getWsUrl(userToken, user);
        Log.d(TAG, "WebSocket URL : " + wsUrl);
        return new StreamWebSocketService(wsUrl, listener);
    }

    @NotNull
    public String getWsUrl(String userToken, User user) throws UnsupportedEncodingException {
        String json = buildUserDetailJSON(user).toString();

        try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.toString());
            return apiClientOptions.getWssURL() + "connect?json=" + json + "&api_key="
                    + apiKey + "&authorization=" + userToken + "&stream-auth-type=" + "jwt";
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new UnsupportedEncodingException("Unable to encode user details json: " + json);
        }
    }

    private JSONObject buildUserDetailJSON(User user) {
        HashMap<String, Object> jsonParameter = new HashMap<>();
        HashMap<String, Object> userDetails = new HashMap<>();

        if (user.getExtraData() != null) {
            userDetails = new HashMap<>(user.getExtraData());
        }

        userDetails.put("id", user.getId());
        userDetails.put("name", user.getName());
        userDetails.put("image", user.getImage());

        jsonParameter.put("user_details", userDetails);
        jsonParameter.put("user_id", user.getId());
        jsonParameter.put("server_determines_connection_id", true);
        return new JSONObject(jsonParameter);
    }
}
