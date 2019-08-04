package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.GetRepliesResponse;

public interface GetRepliesCallback {
    void onSuccess(GetRepliesResponse response);

    void onError(String errMsg, int errCode);
}
