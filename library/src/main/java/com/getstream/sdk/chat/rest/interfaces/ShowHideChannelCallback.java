package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.ShowHideChannelResponse;

public interface ShowHideChannelCallback {
    void onSuccess(ShowHideChannelResponse response);

    void onError(String errMsg, int errCode);
}
