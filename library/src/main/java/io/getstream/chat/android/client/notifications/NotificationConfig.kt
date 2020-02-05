package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.notifications.options.NotificationOptions

data class NotificationConfig(
    val notificationOptions: NotificationOptions,
    var deviceRegisteredListener: DeviceRegisteredListener? = null,
    var messageListener: NotificationMessageLoadListener? = null
)