package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.Event;

public interface ChannelEventHandler {
    void handleEventResponse(Event event);
    void handleConnection();
    void onConnectionFailed(String errMsg, int errCode);
}
