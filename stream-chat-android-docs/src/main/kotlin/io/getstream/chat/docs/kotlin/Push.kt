package io.getstream.chat.docs.kotlin

import android.content.Context
import android.content.Intent
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Device
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.docs.MainActivity

class Push(val context: Context, val client: ChatClient) {

    /**
     * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin">Android & Firebase</a>
     */
    inner class AndroidAndFirebase {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin#registering-a-device-at-stream-backend">Registering a device at Stream Backend</a>
         */
        fun registeringDevice() {
            client.addDevice("firebase-token").enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully registered
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin#setting-up-notification-data-payload-at-stream-dashboard">Setting up notification</a>
         */
        fun setupNotifications() {
            val notificationsConfig = NotificationConfig(
                firebaseMessageIdKey = "message_id",
                firebaseChannelIdKey = "channel_id",
                firebaseChannelTypeKey = "channel_type",
            )

            val notificationHandler = MyNotificationHandler(context, notificationsConfig)

            ChatClient.Builder("{{ api_key }}", context)
                .notifications(ChatNotificationHandler(context, notificationsConfig))
                .notifications(notificationHandler)
                .build()
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=kotlin#redirection-from-notification-to-app">Redirection from notification to app
     </a>
     */
    class MyNotificationHandler(context: Context, notificationConfig: NotificationConfig) :
        ChatNotificationHandler(context, notificationConfig) {

        override fun getNewMessageIntent(
            messageId: String,
            channelType: String,
            channelId: String
        ): Intent = Intent(context, MainActivity::class.java).apply {
            putExtra(EXTRA_CHANNEL_ID, channelId)
            putExtra(EXTRA_CHANNEL_TYPE, channelType)
            putExtra(EXTRA_MESSAGE_ID, messageId)
        }

        companion object {
            const val EXTRA_CHANNEL_ID = "extra_channel_id"
            const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
            const val EXTRA_MESSAGE_ID = "extra_message_id"
        }
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/android/push_android/?language=kotlin#handling-notifications-from-multiple-backend-services">Handling notifications from multiple backend services</a>
     */
    // class CustomFirebaseMessagingService : FirebaseMessagingService() {
    //     private val pushDataSyncHandler: PushMessageSyncHandler =
    //         PushMessageSyncHandler(this)
    //
    //     override fun onNewToken(token: String) {
    //         // update device's token on Stream backend
    //         pushDataSyncHandler.onNewToken(token)
    //     }
    //
    //     override fun onMessageReceived(message: RemoteMessage) {
    //         if (pushDataSyncHandler.isStreamMessage(message)) {
    //             // handle RemoteMessage sent from Stream backend
    //             pushDataSyncHandler.onMessageReceived(message)
    //         } else {
    //             // handle RemoteMessage from other source
    //         }
    //         stopSelf()
    //     }
    // }

    /**
     * @see <a href="https://getstream.io/chat/docs/push_devices/?language=kotlin">Device</a>
     */
    inner class Device_ {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=kotlin#register-a-device">Register a Device</a>
         */
        fun registerADevice() {
            client.addDevice("firebase-token").enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully registered
                } else {
                    // Handle result.error()
                }
            }
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/push_devices/?language=kotlin#unregister-a-device">Unregister a Device</a>
         */
        fun unregisterADevice() {
            client.deleteDevice("firebase-token").enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully unregistered
                } else {
                    // Handle result.error()
                }
            }
        }

        fun listDevices() {
            client.getDevices().enqueue { result ->
                if (result.isSuccess) {
                    val devices: List<Device> = result.data()
                } else {
                    // Handle result.error()
                }
            }
        }
    }
}
