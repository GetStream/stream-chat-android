package io.getstream.chat.sample.application

import android.content.Context
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.sample.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class ChatInitializer(private val context: Context) {

    var client: ChatClient? = null
    var domain: ChatDomain? = null
    var ux: ChatUI? = null

    fun init(apiKey: String) {
        disconnectChatsIfNecessary()

        val notificationConfig =
            NotificationConfig(
                firebaseMessageIdKey = "message_id",
                firebaseChannelIdKey = "channel_id",
                firebaseChannelTypeKey = "channel_type",
                smallIcon = R.drawable.ic_chat_bubble
            )
        val notificationHandler = SampleNotificationHandler(context, notificationConfig)

        client =
            ChatClient.Builder(apiKey, context).loggerHandler(FirebaseLogger).notifications(notificationHandler)
                .logLevel(ChatLogLevel.ALL).build()

        domain =
            ChatDomain.Builder(client!!, context).offlineEnabled().notificationConfig(notificationConfig).build()
        ux = ChatUI.Builder(client!!, domain!!, context).build()
    }

    private fun disconnectChatsIfNecessary() {
        client?.disconnect()
        GlobalScope.launch (Dispatchers.Main) { domain?.disconnect() }
    }
}

