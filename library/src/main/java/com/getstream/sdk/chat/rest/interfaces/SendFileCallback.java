package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.FileSendResponse;

public interface SendFileCallback {
    void onSuccess(FileSendResponse response);

    void onError(String errMsg, int errCode);
}
