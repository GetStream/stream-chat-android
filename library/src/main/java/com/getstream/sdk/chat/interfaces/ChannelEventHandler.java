package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.Event;

public interface ChannelEventHandler {
    void handleEventWSResponse(Event event);
    void onFailed(String errMsg, int errCode);
}
