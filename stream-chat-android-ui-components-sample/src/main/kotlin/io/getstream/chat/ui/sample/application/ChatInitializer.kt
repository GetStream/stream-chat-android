package io.getstream.chat.ui.sample.application

import android.content.Context
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.ui.sample.R

class ChatInitializer(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String, user: User? = null) {
        val notificationConfig =
            NotificationConfig(
                firebaseMessageIdKey = "message_id",
                firebaseChannelIdKey = "channel_id",
                firebaseChannelTypeKey = "channel_type",
                smallIcon = R.drawable.ic_chat_bubble
            )
        val notificationHandler = SampleNotificationHandler(context, notificationConfig)

        val client = ChatClient.Builder(apiKey, context).loggerHandler(FirebaseLogger).notifications(notificationHandler).logLevel(ChatLogLevel.ALL).build()
        val domain = ChatDomain.Builder(client, user, context).offlineEnabled().build()
        val ui = ChatUI.Builder(context).build()
    }

    fun isUserSet(): Boolean {
        return ChatClient.isInitialized && ChatClient.instance().getCurrentUser() != null
    }
}
