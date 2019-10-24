package com.getstream.sdk.chat.rest.utils;

import com.getstream.sdk.chat.interfaces.WSResponseHandler;
import com.getstream.sdk.chat.rest.WebSocketService;

/*
 * Created by Anton Bevza on 2019-10-24.
 */
public class TestWebSocketService extends WebSocketService {

    public TestWebSocketService(String wsURL, String userID, WSResponseHandler webSocketListener) {
        super(wsURL, userID, webSocketListener);
    }

    @Override
    public void connect() {
        //ignore
    }
}
