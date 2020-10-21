package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.errors.ChatError

public interface DeviceRegisteredListener {
    public fun onDeviceRegisteredSuccess()
    public fun onDeviceRegisteredError(error: ChatError)
}
