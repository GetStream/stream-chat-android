package io.getstream.chat.android.client.notifications

import android.content.Context
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.notifications.options.NotificationOptions
import io.getstream.chat.android.client.notifications.options.StreamNotificationOptions

interface ChatNotificationsManager {
    fun setFirebaseToken(
        firebaseToken: String,
        context: Context
    )

    fun onReceiveFirebaseMessage(
        remoteMessage: RemoteMessage,
        context: Context
    )

    fun onReceiveWebSocketEvent(event: ChatEvent, context: Context)

    fun handleRemoteMessage(
        context: Context,
        remoteMessage: RemoteMessage?
    )

    fun handleEvent(context: Context, event: ChatEvent?)

    fun setFailMessageListener(failMessageListener: NotificationMessageLoadListener)

    fun setDeviceRegisterListener(deviceRegisteredListener: DeviceRegisteredListener)

    class Builder {
        private var notificationOptions: NotificationOptions = StreamNotificationOptions()
        private var deviceRegisteredListener: DeviceRegisteredListener? = null
        private var messageListener: NotificationMessageLoadListener? = null

        fun setNotificationOptions(notificationOptions: NotificationOptions): Builder {
            this.notificationOptions = notificationOptions
            return this
        }

        fun setRegisterListener(deviceRegisteredListener: DeviceRegisteredListener): Builder {
            this.deviceRegisteredListener = deviceRegisteredListener
            return this
        }

        fun setNotificationMessageLoadListener(messageListener: NotificationMessageLoadListener) : Builder {
            this.messageListener = messageListener
            return this
        }

        fun build() = NotificationConfig(
            notificationOptions, deviceRegisteredListener, messageListener
        )
    }
}