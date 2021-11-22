package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import io.getstream.chat.android.client.models.Device

/**
 * Generator responsible for providing information needed to register the push notifications provider
 */
public interface PushDeviceGenerator {
    /**
     * Checks if push notification provider is valid for this device
     */
    public fun isValidForThisDevice(context: Context): Boolean

    /**
     * Asynchronously generates a [Device] and calls [onDeviceGenerated] callback once it's ready
     */
    public fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit)
}
