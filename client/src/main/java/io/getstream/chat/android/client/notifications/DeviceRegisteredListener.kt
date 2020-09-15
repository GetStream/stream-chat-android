package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.errors.ChatError

interface DeviceRegisteredListener {
    fun onDeviceRegisteredSuccess()
    fun onDeviceRegisteredError(error: ChatError)
}
