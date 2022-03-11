package io.getstream.chat.android.compose.sample

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.compose.sample.data.UserCredentials
import io.getstream.chat.android.compose.sample.ui.StartupActivity
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.experimental.errorhandler.factory.OfflineErrorHandlerFactoriesProvider
import io.getstream.chat.android.offline.experimental.plugin.configuration.Config
import io.getstream.chat.android.offline.experimental.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator

/**
 * A helper class that is responsible for initializing the SDK and connecting/disconnecting
 * a user. Under the hood, it persists the user so that we are able to connect automatically
 * next time the app is launched.
 */
@OptIn(InternalStreamChatApi::class)
object ChatHelper {

    /**
     * Initializes the SDK with the given API key.
     */
    fun initializeSdk(context: Context, apiKey: String) {
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(FirebasePushDeviceGenerator())
        )
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            newMessageIntent = { _: String, channelType: String, channelId: String ->
                StartupActivity.createIntent(
                    context = context,
                    channelId = "$channelType:$channelId"
                )
            }
        )

        val offlinePlugin = StreamOfflinePluginFactory(Config(userPresence = true, persistenceEnabled = true), context)

        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        ChatClient.Builder(apiKey, context)
            .notifications(notificationConfig, notificationHandler)
            .withPlugin(offlinePlugin)
            .withErrorHandlers(OfflineErrorHandlerFactoriesProvider.createErrorHandlerFactories())
            .logLevel(logLevel)
            .build()
    }

    /**
     * Initializes [ChatClient] with the given user and saves it to the persistent storage.
     */
    fun connectUser(
        userCredentials: UserCredentials,
        onSuccess: () -> Unit = {},
        onError: (ChatError) -> Unit = {},
    ) {
        ChatApp.credentialsRepository.saveUserCredentials(userCredentials)

        ChatClient.instance().connectUser(userCredentials.user, userCredentials.token)
            .enqueue { result ->
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    onError(result.error())
                }
            }
    }

    /**
     * Logs out the user and removes their credentials from the persistent storage.
     */
    fun disconnectUser() {
        ChatApp.credentialsRepository.clearCredentials()

        ChatClient.instance().disconnect()
    }
}
