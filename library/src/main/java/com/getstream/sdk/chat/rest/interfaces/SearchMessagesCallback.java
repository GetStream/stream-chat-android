package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.SearchMessagesResponse;

public interface SearchMessagesCallback {
    void onSuccess(SearchMessagesResponse response);

    void onError(String errMsg, int errCode);
}
