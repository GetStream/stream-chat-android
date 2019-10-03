package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.CompletableResponse;

public interface CompletableCallback {
    void onSuccess(CompletableResponse response);

    void onError(String errMsg, int errCode);
}
