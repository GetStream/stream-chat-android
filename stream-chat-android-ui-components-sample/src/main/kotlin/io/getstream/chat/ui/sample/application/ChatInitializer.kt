package io.getstream.chat.ui.sample.application

import android.content.Context
import com.google.firebase.FirebaseApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.feature.HostActivity



class ChatInitializer(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        FirebaseApp.initializeApp(context)
        val notificationHandler = NotificationHandlerFactory(newMessageIntent = {
                messageId: String,
                channelType: String,
                channelId: String,
            ->
            HostActivity.createLaunchIntent(context, messageId, channelType, channelId)
        }).createNotificationHandler(context)
        val notificationConfig =
            NotificationConfig(
                pushDeviceGenerators = listOf(
                    // HuaweiPushDeviceGenerator(context, ApplicationConfigurator.HUAWEI_APP_ID),
                    FirebasePushDeviceGenerator()
                ),
            )
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING
        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationConfig, notificationHandler)
            .logLevel(logLevel)
            .build()

        val domain = ChatDomain.Builder(client, context)
            .userPresenceEnabled()
            .offlineEnabled()
            .build()
    }
}
