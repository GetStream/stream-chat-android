package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.FlagResponse;

public interface FlagCallback {
    void onSuccess(FlagResponse response);

    void onError(String errMsg, int errCode);
}
