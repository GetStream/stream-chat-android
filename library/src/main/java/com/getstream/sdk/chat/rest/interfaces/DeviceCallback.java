package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.DevicesResponse;

public interface DeviceCallback {
    void onSuccess(DevicesResponse response);

    void onError(String errMsg, int errCode);
}
