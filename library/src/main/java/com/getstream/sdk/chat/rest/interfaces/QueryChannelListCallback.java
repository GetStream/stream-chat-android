package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.QueryChannelsResponse;

public interface QueryChannelListCallback {
    void onSuccess(QueryChannelsResponse response);

    void onError(String errMsg, int errCode);
}
