package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.channel.Event;

public interface EventHandler {
    void handleEvent(Event event);

    void handleReconnection(boolean disconnect);
}
