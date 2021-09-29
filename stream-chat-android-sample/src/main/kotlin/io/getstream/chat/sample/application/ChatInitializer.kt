package io.getstream.chat.sample.application

import android.content.Context
import android.content.Intent
import com.getstream.sdk.chat.ChatUI
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.sample.BuildConfig
import io.getstream.chat.sample.R
import io.getstream.chat.sample.feature.HostActivity

const val EXTRA_CHANNEL_ID = "extra_channel_id"
const val EXTRA_CHANNEL_TYPE = "extra_channel_type"
const val EXTRA_MESSAGE_ID = "extra_message_id"
class ChatInitializer(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        NotificationHandlerFactory.newMessageIntent = {
                messageId: String,
                channelType: String,
                channelId: String
            ->
            Intent(context, HostActivity::class.java).apply {
                putExtra(EXTRA_CHANNEL_ID, channelId)
                putExtra(EXTRA_CHANNEL_TYPE, channelType)
                putExtra(EXTRA_MESSAGE_ID, messageId)

            }
        }
        val notificationConfig =
            NotificationConfig(
                smallIcon = R.drawable.ic_chat_bubble,
                pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
            )
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationConfig)
            .logLevel(logLevel)
            .build()

        val domain = ChatDomain.Builder(client, context)
            .offlineEnabled()
            .build()

        val ui = ChatUI.Builder(context).build()
    }

    fun isUserSet(): Boolean {
        return ChatClient.isInitialized && ChatClient.instance().getCurrentUser() != null
    }
}
