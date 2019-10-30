package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;

public interface WebSocketService {

    void connect();

    void disconnect();

    WSResponseHandler getWebSocketListener();

}
