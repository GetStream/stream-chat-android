package io.getstream.chat.android.client.notifications

import io.getstream.chat.android.client.notifications.options.ChatNotificationOptions
import io.getstream.chat.android.client.notifications.options.NotificationOptions

data class ChatNotificationConfig(
    val notificationOptions: NotificationOptions,
    var deviceRegisteredListener: DeviceRegisteredListener? = null,
    var messageListener: NotificationMessageLoadListener? = null
){
    class Builder {
        private var notificationOptions: NotificationOptions = ChatNotificationOptions()
        private var deviceRegisteredListener: DeviceRegisteredListener? = null
        private var messageListener: NotificationMessageLoadListener? = null

        fun options(notificationOptions: NotificationOptions): Builder {
            this.notificationOptions = notificationOptions
            return this
        }

        fun registerListener(deviceRegisteredListener: DeviceRegisteredListener): Builder {
            this.deviceRegisteredListener = deviceRegisteredListener
            return this
        }

        fun messageLoadListener(messageListener: NotificationMessageLoadListener) : Builder {
            this.messageListener = messageListener
            return this
        }

        fun build() = ChatNotificationConfig(
            notificationOptions, deviceRegisteredListener, messageListener
        )
    }
}