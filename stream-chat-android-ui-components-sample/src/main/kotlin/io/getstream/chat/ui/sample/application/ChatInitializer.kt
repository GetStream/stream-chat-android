package io.getstream.chat.ui.sample.application

import android.content.Context
import com.google.firebase.FirebaseApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.pushprovider.huawei.HuaweiPushDeviceGenerator
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.R

@OptIn(InternalStreamChatApi::class)
class ChatInitializer(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        FirebaseApp.initializeApp(context)
        val notificationConfig =
            NotificationConfig(
                smallIcon = R.drawable.ic_chat_bubble,
                loadNotificationDataIcon = R.drawable.ic_chat_bubble,
                pushDeviceGenerators = listOf(
                    HuaweiPushDeviceGenerator(context, ApplicationConfigurator.HUAWEI_APP_ID),
                    FirebasePushDeviceGenerator()
                ),
            )
        val notificationHandler = SampleNotificationHandler(context, notificationConfig)
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationHandler)
            .logLevel(logLevel)
            .build()

        val domain = ChatDomain.Builder(client, context)
            .userPresenceEnabled()
            .offlineEnabled()
            .build()
    }
}
