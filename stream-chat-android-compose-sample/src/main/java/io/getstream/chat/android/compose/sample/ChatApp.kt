package io.getstream.chat.android.compose.sample

import android.app.Application
import com.getstream.sdk.chat.utils.DateFormatter
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.Config
import io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator

@OptIn(InternalStreamChatApi::class)
class ChatApp : Application() {

    companion object {
        lateinit var dateFormatter: DateFormatter
            private set
    }

    override fun onCreate() {
        super.onCreate()
        dateFormatter = DateFormatter.from(this)

        setupStreamSdk()
        initializeToggleService()
        connectUser()
    }

    @OptIn(InternalStreamChatApi::class)
    private fun initializeToggleService() {
        ToggleService.init(applicationContext, mapOf(ToggleService.TOGGLE_KEY_OFFLINE to BuildConfig.DEBUG))
    }

    @OptIn(ExperimentalStreamChatApi::class)
    private fun setupStreamSdk() {
        val offlinePlugin = OfflinePlugin(Config(userPresence = true, persistenceEnabled = true))

        ChatClient.Builder("qx5us2v6xvmh", applicationContext)
            .logLevel(ChatLogLevel.ALL)
            .notifications(createNotificationConfig(), createNotificationHandler())
            .withPlugin(offlinePlugin)
            .build()
    }

    private fun connectUser() {
        ChatClient.instance().connectUser(
            user = User(
                id = "filip",
                extraData = mutableMapOf(
                    "name" to "Filip Babić",
                    "image" to "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128",
                ),
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiZmlsaXAifQ.WKqTjU6fHHjtFej-sUqS2ml3Rvdqn4Ptrf7jfKqzFgU"
        ).enqueue()
    }

    private fun createNotificationConfig(): NotificationConfig {
        return NotificationConfig(
            pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
        )
    }

    private fun createNotificationHandler(): NotificationHandler {
        return NotificationHandlerFactory.createNotificationHandler(
            context = this,
            newMessageIntent = { _: String, channelType: String, channelId: String ->
                PushHandlerActivity.getIntent(
                    context = this,
                    channelId = "$channelType:$channelId"
                )
            }
        )
    }
}
