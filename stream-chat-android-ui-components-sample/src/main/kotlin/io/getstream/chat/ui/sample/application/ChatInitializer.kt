package io.getstream.chat.ui.sample.application

import android.content.Context
import com.google.firebase.FirebaseApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.pushprovider.huawei.HuaweiPushDeviceGenerator
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.feature.HostActivity

@OptIn(InternalStreamChatApi::class)
class ChatInitializer(private val context: Context) {

    @OptIn(ExperimentalStreamChatApi::class)
    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        FirebaseApp.initializeApp(context)
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            newMessageIntent = {
                    messageId: String,
                    channelType: String,
                    channelId: String,
                ->
                HostActivity.createLaunchIntent(context, messageId, channelType, channelId)
            }
        )
        val notificationConfig =
            NotificationConfig(
                pushDeviceGenerators = listOf(
                    FirebasePushDeviceGenerator(),
                    HuaweiPushDeviceGenerator(context, ApplicationConfigurator.HUAWEI_APP_ID),
                ),
            )
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        val offlinePlugin = OfflinePlugin(Config(userPresence = true, persistenceEnabled = true))

        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationConfig, notificationHandler)
            .logLevel(logLevel)
            .withPlugin(offlinePlugin)
            .build()
    }
}
