package com.getstream.sdk.chat.notifications;

import org.jetbrains.annotations.NotNull;

public interface DeviceRegisteredListener {

    /**
     * Callback called when device registered on server successfully
     */
    void onDeviceRegisteredSuccess();

    /**
     * Callback called when we can't register device on server
     *
     * @param errorMessage - Message from server
     * @param errorCode    - error code from server
     */
    void onDeviceRegisteredError(@NotNull String errorMessage, int errorCode);
}
