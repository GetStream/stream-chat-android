package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import io.getstream.chat.android.client.models.Device

public interface PushDeviceGenerator {
    public fun isValidForThisDevice(context: Context): Boolean
    public fun asyncGenerateDevice(onDeviceGenerated: (device: Device) -> Unit)
}
