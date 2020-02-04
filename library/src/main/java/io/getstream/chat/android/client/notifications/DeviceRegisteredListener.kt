package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.errors.ChatError

interface DeviceRegisteredListener {
    /**
     * Callback called when device registered on server successfully
     */
    fun onDeviceRegisteredSuccess()

    /**
     * Callback called when we can't register device on server
     *
     * @param errorMessage - Message from server
     * @param errorCode    - error code from server
     */
    fun onDeviceRegisteredError(error: ChatError)
}