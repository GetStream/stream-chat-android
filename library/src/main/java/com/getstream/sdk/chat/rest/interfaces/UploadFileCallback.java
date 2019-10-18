package com.getstream.sdk.chat.rest.interfaces;

public interface UploadFileCallback<RESPONSE, PROGRESS> {

    void onSuccess(RESPONSE response);

    void onError(String errMsg, int errCode);

    void onProgress(PROGRESS progress);
}
