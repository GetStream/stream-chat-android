package com.getstream.sdk.chat.rest.interfaces;

import com.getstream.sdk.chat.rest.response.AddDevicesResponse;

public interface AddDeviceCallback {
    void onSuccess(AddDevicesResponse response);

    void onError(String errMsg, int errCode);
}
