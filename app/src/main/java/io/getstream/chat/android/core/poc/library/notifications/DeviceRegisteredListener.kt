package io.getstream.chat.android.core.poc.library.notifications

import io.getstream.chat.android.core.poc.library.errors.ChatError

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