package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.ChannelState;

public interface QueryWatchCallback {
    void onSuccess(ChannelState response);

    void onError(String errMsg, int errCode);
}
