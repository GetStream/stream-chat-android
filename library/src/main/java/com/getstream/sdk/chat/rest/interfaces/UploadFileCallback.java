package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.UploadFileResponse;

public interface UploadFileCallback {
    void onSuccess(UploadFileResponse response);

    void onError(String errMsg, int errCode);

    void onProgress(int percentage);
}
