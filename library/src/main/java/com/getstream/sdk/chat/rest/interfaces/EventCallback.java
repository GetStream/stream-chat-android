package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.EventResponse;

public interface EventCallback {
    void onSuccess(EventResponse response);

    void onError(String errMsg, int errCode);
}
