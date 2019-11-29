package com.getstream.sdk.chat.rest.core.providers;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.WebSocketService;

import java.io.UnsupportedEncodingException;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public interface WebSocketServiceProvider {
    WebSocketService provideWebSocketService(User user, String userToken, WSResponseHandler listener) throws UnsupportedEncodingException;
    WebSocketService provideAnonymousWebSocketService(User user, WSResponseHandler listener) throws UnsupportedEncodingException;
}
