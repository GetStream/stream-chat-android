package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.Event;

public interface WSResponseHandler {
    void onWSEvent(Event event);

    void connectionResolved(Event event);

    void connectionRecovered();
}
