package io.getstream.chat.android.client.notifications.handler

/**
 * Push notifications configuration class
 */
public data class NotificationConfig(
    /**
     * Enables/disables push notifications on the device.
     * Device's token won't be registered if push notifications are disabled.
     */
    val pushNotificationsEnabled: Boolean = true,

    /**
     * A list of generators responsible for providing the information needed to register a device
     * @see [PushDeviceGenerator]
     */
    val pushDeviceGenerators: List<PushDeviceGenerator> = listOf(),
)
