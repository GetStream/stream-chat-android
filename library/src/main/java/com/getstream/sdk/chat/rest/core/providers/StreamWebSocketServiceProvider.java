package com.getstream.sdk.chat.rest.core.providers;

import android.util.Log;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.rest.StreamWebSocketService;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.Client;

import org.jetbrains.annotations.Nullable;

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
    public WebSocketService provideWebSocketService(User user, @Nullable String userToken, WSResponseHandler listener, boolean anonymousAuth) throws UnsupportedEncodingException {
        String wsUrl = getWsUrl(userToken, user, anonymousAuth);
        Log.d(TAG, "WebSocket URL : " + wsUrl);
        return new StreamWebSocketService(wsUrl, listener);
    }

    public String getWsUrl(String userToken, User user, boolean anonymousAuth) throws UnsupportedEncodingException {
        if (anonymousAuth && userToken != null) {
            Log.e(TAG, "Can\'t use anonymousAuth with userToken. UserToken will be ignored");
        }
        if (!anonymousAuth && userToken == null) {
            throw new IllegalArgumentException("userToken must be non-null in not anonymous mode");
        }

        String json = buildUserDetailJSON(user);

        try {
            json = URLEncoder.encode(json, StandardCharsets.UTF_8.toString());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new UnsupportedEncodingException("Unable to encode user details json: " + json);
        }

        String baseWsUrl = apiClientOptions.getWssURL() + "connect?json=" + json + "&api_key=" + apiKey;
        if (anonymousAuth) {
            return baseWsUrl + "&stream-auth-type=" + "anonymous";
        } else {
            return baseWsUrl + "&authorization=" + userToken + "&stream-auth-type=" + "jwt";
        }
    }

    public String buildUserDetailJSON(User user) {
        HashMap<String, Object> data = new HashMap<>();
        data.put("user_details", user);
        data.put("server_determines_connection_id", true);
        data.put("user_id", user.getId());
        return GsonConverter.Gson().toJson(data);
    }
}
