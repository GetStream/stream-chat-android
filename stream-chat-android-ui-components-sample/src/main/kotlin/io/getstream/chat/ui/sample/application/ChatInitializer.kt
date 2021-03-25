package io.getstream.chat.ui.sample.application

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.R

class ChatInitializer(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        val notificationConfig =
            NotificationConfig(
                firebaseMessageIdKey = "message_id",
                firebaseChannelIdKey = "channel_id",
                firebaseChannelTypeKey = "channel_type",
                smallIcon = R.drawable.ic_chat_bubble
            )
        val notificationHandler = SampleNotificationHandler(context, notificationConfig)
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationHandler)
            .logLevel(logLevel)
            .useNewSerialization(true)
            .build()

        val domain = ChatDomain.Builder(client, context)
            .userPresenceEnabled()
            .offlineEnabled()
            .build()
    }

    fun isUserSet(): Boolean {
        return ChatClient.isInitialized && ChatClient.instance().getCurrentUser() != null
    }
}
