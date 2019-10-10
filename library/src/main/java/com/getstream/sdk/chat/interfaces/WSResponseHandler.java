package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.Event;
import com.getstream.sdk.chat.rest.response.WsErrorMessage;

public interface WSResponseHandler {
    void onWSEvent(Event event);

    void connectionResolved(Event event);

    void connectionRecovered();

    void tokenExpired();

    void onError(WsErrorMessage error);
}
