package com.getstream.sdk.chat.notifications;

public interface DeviceRegisteredListener {

    void onDeviceRegisteredSuccess();
    void onDeviceRegisteredError(String errorMessage, Integer errorCode);
}
