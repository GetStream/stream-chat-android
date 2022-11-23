package io.getstream.android.push

import android.content.Context

/**
 * Generator responsible for providing information needed to register the push notifications provider
 */
public interface PushDeviceGenerator {
    /**
     * Checks if push notification provider is valid for this device
     */
    public fun isValidForThisDevice(context: Context): Boolean

    /**
     * Called when this [PushDeviceGenerator] has been selected to be used.
     */
    public fun onPushDeviceGeneratorSelected()

    /**
     * Asynchronously generates a [PushDevice] and calls [onPushDeviceGenerated] callback once it's ready
     */
    public fun asyncGeneratePushDevice(onPushDeviceGenerated: (pushDevice: PushDevice) -> Unit)
}