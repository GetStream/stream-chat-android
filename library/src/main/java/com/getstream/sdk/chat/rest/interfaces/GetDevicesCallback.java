package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.GetDevicesResponse;

public interface GetDevicesCallback {
    void onSuccess(GetDevicesResponse response);

    void onError(String errMsg, int errCode);
}
