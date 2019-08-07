package com.getstream.sdk.chat.interfaces;

public interface ClientConnectionCallback {
    void onSuccess();

    void onError(String errMsg, int errCode);
}
