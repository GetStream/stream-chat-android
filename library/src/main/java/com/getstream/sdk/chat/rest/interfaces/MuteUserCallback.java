package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.MuteUserResponse;

public interface MuteUserCallback {
    void onSuccess(MuteUserResponse response);

    void onError(String errMsg, int errCode);
}
