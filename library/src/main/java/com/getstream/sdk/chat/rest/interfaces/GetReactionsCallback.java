package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.GetReactionsResponse;

public interface GetReactionsCallback {
    void onSuccess(GetReactionsResponse response);

    void onError(String errMsg, int errCode);
}
