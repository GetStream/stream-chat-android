package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.rest.User;

public interface ClientConnectionCallback {
    void onSuccess(User user);

    void onError(String errMsg, int errCode);
}
