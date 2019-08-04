package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.model.Event;

import okio.ByteString;

public interface WSResponseHandler {
    void handleEventWSResponse(Event event);
    void handleByteStringWSResponse(ByteString byteString);
    void onFailed(String errMsg, int errCode);
}
