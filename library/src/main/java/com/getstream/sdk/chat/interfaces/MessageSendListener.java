package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.rest.apimodel.response.MessageResponse;

public interface MessageSendListener {
    void onSuccess(MessageResponse response);

    void onFailed(String errMsg, int errCode);
}
