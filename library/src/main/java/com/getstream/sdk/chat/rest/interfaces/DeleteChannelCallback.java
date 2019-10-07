package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.DeleteChannelResponse;

public interface DeleteChannelCallback {
    void onSuccess(DeleteChannelResponse response);

    void onError(String errMsg, int errCode);
}
