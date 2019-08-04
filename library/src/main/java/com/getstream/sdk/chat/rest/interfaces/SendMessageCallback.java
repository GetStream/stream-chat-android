package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.MessageResponse;

public interface SendMessageCallback {
    void onSuccess(MessageResponse response);

    void onError(String errMsg, int errCode);
}
