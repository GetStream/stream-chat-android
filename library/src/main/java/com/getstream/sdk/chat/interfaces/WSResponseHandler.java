package com.getstream.sdk.chat.interfaces;

public interface WSResponseHandler {
    void handleWSResponse(Object response);
    void onFailed(String errMsg, int errCode);
}
