@file:Suppress("unused", "UNUSED_ANONYMOUS_PARAMETER", "ControlFlowWithEmptyBody")

package io.getstream.chat.docs.kotlin.client.docusaurus

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.huawei.hms.push.HmsMessageService
import com.xiaomi.mipush.sdk.MiPushCommandMessage
import com.xiaomi.mipush.sdk.MiPushMessage
import com.xiaomi.mipush.sdk.PushMessageReceiver
import io.getstream.android.push.firebase.FirebaseMessagingDelegate
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.android.push.huawei.HuaweiMessagingDelegate
import io.getstream.android.push.huawei.HuaweiPushDeviceGenerator
import io.getstream.android.push.permissions.NotificationPermissionStatus
import io.getstream.android.push.xiaomi.XiaomiMessagingDelegate
import io.getstream.android.push.xiaomi.XiaomiPushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory

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
            notificationConfig = notificationConfig,
            newMessageIntent = {
                    message: Message,
                    channel: Channel,
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
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#customize-notification-style">Customizing Notification Style</a>
     */
    fun customizeNotificationStyle(context: Context, notificationConfig: NotificationConfig) {
        val notificationChannelId = ""
        val notificationId = 1

        class MyNotificationHandler(private val context: Context) : NotificationHandler {
            private val notificationManager: NotificationManager by lazy {
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            }

            override fun onNotificationPermissionStatus(status: NotificationPermissionStatus) {
                when (status) {
                    NotificationPermissionStatus.REQUESTED -> {
                        // invoked when POST_NOTIFICATIONS permission is requested
                    }
                    NotificationPermissionStatus.GRANTED -> {
                        // invoked when POST_NOTIFICATIONS permission is granted
                    }
                    NotificationPermissionStatus.DENIED -> {
                        // invoked when POST_NOTIFICATIONS permission is denied
                    }
                    NotificationPermissionStatus.RATIONALE_NEEDED -> {
                        // invoked when POST_NOTIFICATIONS permission requires rationale
                    }
                }
            }

            override fun showNotification(channel: Channel, message: Message) {
                val notification = NotificationCompat.Builder(context, notificationChannelId)
                    .build()
                notificationManager.notify(notificationId, notification)
            }

            override fun dismissChannelNotifications(channelType: String, channelId: String) {
                // Dismiss all notification related with this channel
            }

            override fun dismissAllNotifications() {
                // Dismiss all notifications
            }
        }

        val notificationHandler = MyNotificationHandler(context)

        ChatClient.Builder("api-key", context)
            .notifications(notificationConfig, notificationHandler)
            .build()
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#dismissing-notifications">Dismissing Notifications</a>
     */
    fun dismissingNotifications() {
        ChatClient.instance().dismissChannelNotifications("messaging", "general")
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/#multi-bundle">Multi Bundle</a>
     */
    fun multiBundle() {
        Device(
            token = "token-generated-by-provider",
            pushProvider = PushProvider.FIREBASE, // your push provider
            providerName = "providerName",
        )
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
                pushDeviceGenerators = listOf(
                    FirebasePushDeviceGenerator(
                        context = context,
                        providerName = "providerName"
                    )
                ),
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
                    FirebaseMessagingDelegate.registerFirebaseToken(token, providerName = "providerName")
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

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei/#huawei-push-kit">Huawei Push Kit</a>
     */
    inner class Huawei {

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/huawei/#receiving-notifications-in-the-client">Receiving Notifications in the Client</a>
         */
        fun configureHuaweiNotifications(context: Context) {
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(
                    HuaweiPushDeviceGenerator(
                        context = context,
                        appId = "YOUR HUAWEI APP ID",
                        providerName = "providerName"
                    )
                )
            )
            ChatClient.Builder("apiKey", context)
                .notifications(notificationConfig)
                .build()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/firebase/#using-a-custom-firebase-messaging-service">Using a Custom Service</a>
         */
        inner class CustomHuaweiMessagingService : HmsMessageService() {

            override fun onNewToken(token: String) {
                // Update device's token on Stream backend
                try {
                    HuaweiMessagingDelegate.registerHuaweiToken(token, "providerName")
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }

            override fun onMessageReceived(message: com.huawei.hms.push.RemoteMessage) {
                try {
                    if (HuaweiMessagingDelegate.handleRemoteMessage(message)) {
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

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi/#xiaomi-mi-push">Xiaomi Mi Push</a>
     */
    inner class Xiaomi {
        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi/#receiving-notifications-in-the-client">Receiving Notifications in the Client</a>
         */
        fun configureXiaomiNotifications(context: Context) {
            val notificationConfig = NotificationConfig(
                pushDeviceGenerators = listOf(
                    XiaomiPushDeviceGenerator(
                        context = context,
                        appId = "YOUR XIAOMI APP ID",
                        appKey = "YOUR XIAOMI APP KEY",
                        providerName = "providerName",
                    )
                )
            )
            ChatClient.Builder("apiKey", context)
                .notifications(notificationConfig)
                .build()
        }

        /**
         * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/push-notifications/xiaomi/#using-a-custom-pushmessagereceiver">Using a Custom PushMessageReceiver</a>
         */
        inner class CustomPushMessageReceiver : PushMessageReceiver() {

            override fun onReceiveRegisterResult(context: Context, miPushCommandMessage: MiPushCommandMessage) {
                // Update device's token on Stream backend
                try {
                    XiaomiMessagingDelegate.registerXiaomiToken(miPushCommandMessage, "providerName")
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }

            override fun onReceivePassThroughMessage(context: Context, miPushMessage: MiPushMessage) {
                try {
                    if (XiaomiMessagingDelegate.handleMiPushMessage(miPushMessage)) {
                        // MiPushMessage was from Stream and it is already processed
                    } else {
                        // MiPushMessage wasn't sent from Stream and it needs to be handled by you
                    }
                } catch (exception: IllegalStateException) {
                    // ChatClient was not initialized
                }
            }
        }
    }
}
