package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.QueryUserListResponse;

public interface QueryUserListCallback {
    void onSuccess(QueryUserListResponse response);

    void onError(String errMsg, int errCode);
}
