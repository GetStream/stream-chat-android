package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.ChannelResponse;

public interface ChannelCallback {
    void onSuccess(ChannelResponse response);

    void onError(String errMsg, int errCode);
}
