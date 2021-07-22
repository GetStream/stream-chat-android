package io.getstream.chat.ui.sample.application

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.offline.plugin.Config
import io.getstream.chat.android.offline.plugin.OfflinePlugin
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.R

class ChatInitializerV2(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        val notificationConfig =
            NotificationConfig(
                firebaseMessageIdKey = "message_id",
                firebaseChannelIdKey = "channel_id",
                firebaseChannelTypeKey = "channel_type",
                smallIcon = R.drawable.ic_chat_bubble,
                loadNotificationDataIcon = R.drawable.ic_chat_bubble,
                shouldGroupNotifications = true,
            )
        val notificationHandler = SampleNotificationHandler(context, notificationConfig)
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        val offlinePlugin = OfflinePlugin(Config(userPresence = true, persistenceEnabled = true))

        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationHandler)
            .logLevel(logLevel)
            .withPlugin(offlinePlugin)
            .build()
    }
}
