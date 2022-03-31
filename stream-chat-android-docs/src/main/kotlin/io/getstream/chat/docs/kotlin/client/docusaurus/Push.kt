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

class Push {

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

    inner class Firebase {
        fun configureFirebaseNotifications(context: Context) {
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
            )
            ChatClient.Builder("apiKey", context)
                .notifications(notificationConfig)
                .build()
        }

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
