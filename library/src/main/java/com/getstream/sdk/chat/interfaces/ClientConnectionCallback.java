package com.getstream.sdk.chat.interfaces;

import com.getstream.sdk.chat.enums.ClientErrorCode;
import com.getstream.sdk.chat.rest.User;

public interface ClientConnectionCallback {

    void onSuccess(User user);

    /**
     *
     * @param errMsg Human readable message
     * @param errCode http status code or {@link ClientErrorCode}
     */
    void onError(String errMsg, int errCode);
}
