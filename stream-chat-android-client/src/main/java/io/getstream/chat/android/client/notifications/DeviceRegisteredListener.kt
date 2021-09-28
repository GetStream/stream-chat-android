package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.errors.ChatError

@Deprecated(
    message = "This class is not used anymore",
    level = DeprecationLevel.ERROR,
)
public interface DeviceRegisteredListener {
    public fun onDeviceRegisteredSuccess()
    public fun onDeviceRegisteredError(error: ChatError)
}
