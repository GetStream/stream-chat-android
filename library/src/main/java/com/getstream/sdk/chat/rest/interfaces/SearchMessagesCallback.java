package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.SearchMessagesRespose;

public interface SearchMessagesCallback {
    void onSuccess(SearchMessagesRespose response);

    void onError(String errMsg, int errCode);
}
