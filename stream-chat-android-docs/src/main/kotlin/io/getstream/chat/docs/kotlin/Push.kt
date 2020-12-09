package io.getstream.chat.docs.kotlin

import android.content.Context
import android.util.Log
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.docs.StaticInstances.TAG

class Push(val context: Context) {

    /**
     * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin">Android & Firebase</a>
     */
    inner class AndroidAndFirebase(val client: ChatClient) {

        /**
         * @see <a href="https://getstream.io/chat/docs/push_android/?language=kotlin#registering-a-device-at-stream-backend">Registering a device at Stream Backend</a>
         */
        fun registeringDevice() {
            client.addDevice("firebase-token").enqueue { result ->
                if (result.isSuccess) {
                    // Device was successfully registered
                } else {
                    Log.e(TAG, String.format("There was an error %s", result.error(), result.error().cause))
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
                firebaseChannelTypeKey = "channel_type"
            )

            ChatClient.Builder("{{ api_key }}", context)
                .notifications(ChatNotificationHandler(context, notificationsConfig))
                .build()
        }
    }
}