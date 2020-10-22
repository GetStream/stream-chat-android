package io.getstream.chat.sample.application

import android.content.Context
import com.getstream.sdk.chat.Chat
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.sample.R

class ChatInitializer(private val context: Context) {

    fun init(apiKey: String) {
        Chat.Builder(apiKey, context).apply {
            offlineEnabled = true
            val notificationConfig =
                NotificationConfig(
                    firebaseMessageIdKey = "message_id",
                    firebaseChannelIdKey = "channel_id",
                    firebaseChannelTypeKey = "channel_type",
                    smallIcon = R.drawable.ic_chat_bubble
                )
            notificationHandler = SampleNotificationHandler(context, notificationConfig)
            chatLoggerHandler = SampleLoggingHandler()
        }.build()
    }
}
