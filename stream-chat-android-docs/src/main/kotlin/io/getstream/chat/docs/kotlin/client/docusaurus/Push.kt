@file:Suppress("unused", "UNUSED_ANONYMOUS_PARAMETER", "ControlFlowWithEmptyBody")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.content.Context
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.pushprovider.firebase.FirebaseMessagingDelegate
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/">Push Notifications</a>
 */
class Push {

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#overview">Overview</a>
     */
    fun configureNotification(context: Context, notificationHandler: NotificationHandler) {
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                // PushDeviceGenerator
            ),
        )

        ChatClient.Builder("api-key", context)
            .notifications(notificationConfig, notificationHandler)
            .build()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#using-our-notificationhandlerfactory">Using our NotificationHandlerFactory</a>
     */
    fun customNotificationHandler(context: Context) {
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                // PushDeviceGenerator
            ),
        )

        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            newMessageIntent = {
                messageId: String,
                channelType: String,
                channelId: String,
                ->
                // Return the intent you want to be triggered when the notification is clicked
                val intent = Intent()
                intent
            }
        )

        ChatClient.Builder("api-key", context)
            .notifications(notificationConfig, notificationHandler)
            .build()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/">Firebase Cloud Messaging</a>
     */
    inner class Firebase {

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#receiving-notifications-in-the-client">Receiving Notifications in the Client</a>
         */
        fun configureFirebaseNotifications(context: Context) {
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
            )
            ChatClient.Builder("apiKey", context)
                .notifications(notificationConfig)
                .build()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#using-a-custom-firebase-messaging-service">Using a Custom Firebase Messaging Service</a>
         */
        inner class CustomFirebaseMessagingService : FirebaseMessagingService() {

            override fun onNewToken(token: String) {
                // Update device's token on Stream backend
                try {
                    FirebaseMessagingDelegate.registerFirebaseToken(token)
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }

            override fun onMessageReceived(message: RemoteMessage) {
                try {
                    if (FirebaseMessagingDelegate.handleRemoteMessage(message)) {
                        // RemoteMessage was from Stream and it is already processed
                    } else {
                        // RemoteMessage wasn't sent from Stream and it needs to be handled by you
                    }
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }
        }
    }
}
