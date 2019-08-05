package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.Event;

public interface WSResponseHandler {
    void handleWSEvent(Event event);
    void handleWSConnectReply(Event event);
    void handleWSRecover();
}
