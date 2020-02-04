package io.getstream.chat.android.client.sample

import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.facebook.stetho.Stetho
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.ChatClientBuilder
import io.getstream.chat.android.client.Message
import io.getstream.chat.android.client.api.ApiClientOptions
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.logger.StreamChatLogger
import io.getstream.chat.android.client.logger.StreamLogger
import io.getstream.chat.android.client.logger.StreamLoggerHandler
import io.getstream.chat.android.client.logger.StreamLoggerLevel
import io.getstream.chat.android.client.notifications.DeviceRegisteredListener
import io.getstream.chat.android.client.notifications.NotificationMessageLoadListener
import io.getstream.chat.android.client.notifications.NotificationsManager
import io.getstream.chat.android.client.notifications.StreamNotificationsManager
import io.getstream.chat.android.client.notifications.options.NotificationIntentProvider
import io.getstream.chat.android.client.notifications.options.StreamNotificationOptions
import io.getstream.chat.android.client.sample.cache.AppDatabase
import io.getstream.chat.android.client.sample.common.KeyValue
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryLive
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositoryRx
import io.getstream.chat.android.client.sample.repositories.ChannelsRepositorySync

class App : Application() {

    companion object {
        lateinit var client: ChatClient
        lateinit var channelsRepositorySync: ChannelsRepositorySync
        lateinit var channelsRepositoryRx: ChannelsRepositoryRx
        lateinit var channelsRepositoryLive: ChannelsRepositoryLive
        lateinit var db: AppDatabase
        lateinit var cache: ChannelsCache
        lateinit var keyValue: KeyValue
        lateinit var notificationsManager: NotificationsManager
    }

    private lateinit var logger: StreamLogger

    override fun onCreate() {
        super.onCreate()

        Stetho.initializeWithDefaults(this)

        db = AppDatabase.getInstance(this)

        val apiKey = "d2q3juekvgsf"

        val apiOptions = ApiClientOptions.Builder()
            .baseURL("chat-us-east-staging.stream-io-api.com")
            .cdnUrl("chat-us-east-staging.stream-io-api.com")
            .timeout(10000)
            .cdnTimeout(10000)
            .build()

        setupLogger()

        client = ChatClientBuilder(apiKey, apiOptions, logger).build()
        keyValue = KeyValue(this)
        cache = ChannelsCache(db.channels())
        channelsRepositorySync = ChannelsRepositorySync(client, cache)
        channelsRepositoryRx = ChannelsRepositoryRx(client, cache)
        channelsRepositoryLive = ChannelsRepositoryLive(client, cache)
    }

    private fun setupLogger() {
        val loggerHandler: StreamLoggerHandler = object : StreamLoggerHandler {
            override fun logT(throwable: Throwable) {
                // display throwable logs here
            }

            override fun logT(className: String, throwable: Throwable) {
                // display throwable logs here
            }

            override fun logI(className: String, message: String) {
                // display info logs here
            }

            override fun logD(className: String, message: String) {
                // display debug logs here
            }

            override fun logW(className: String, message: String) {
                // display warning logs here
            }

            override fun logE(className: String, message: String) {
                // display error logs here
            }
        }

        logger = StreamChatLogger.Builder()
            .loggingLevel(if (BuildConfig.DEBUG) StreamLoggerLevel.ALL else StreamLoggerLevel.NOTHING)
            .setLoggingHandler(loggerHandler)
            .build()
    }

    private fun setupNotifications() {
        val notificationOptions = StreamNotificationOptions()

        notificationOptions.setNotificationIntentProvider(
            object : NotificationIntentProvider {
                override fun getIntentForFirebaseMessage(
                    context: Context,
                    remoteMessage: RemoteMessage
                ): PendingIntent? {
                    val payload: Map<String, String> =
                        remoteMessage.getData()
                    val intent = Intent(context, ChannelActivity::class.java)
                    intent.putExtra(
                        io.getstream.chat.example.BaseApplication.EXTRA_CHANNEL_TYPE,
                        payload[StreamNotificationsManager.CHANNEL_TYPE_KEY]
                    )
                    intent.putExtra(
                        io.getstream.chat.example.BaseApplication.EXTRA_CHANNEL_ID,
                        payload[StreamNotificationsManager.CHANNEL_ID_KEY]
                    )
                    return PendingIntent.getActivity(
                        context, 999,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }

                fun getIntentForWebSocketEvent(
                    context: Context,
                    event: ChatEvent
                ): PendingIntent? {
                    val intent = Intent(context, ChannelActivity::class.java)
                    intent.putExtra(
                        io.getstream.chat.example.BaseApplication.EXTRA_CHANNEL_TYPE,
                        StringUtility.getChannelTypeFromCid(event.getCid())
                    )
                    intent.putExtra(
                        io.getstream.chat.example.BaseApplication.EXTRA_CHANNEL_ID,
                        StringUtility.getChannelIdFromCid(event.getCid())
                    )
                    return PendingIntent.getActivity(
                        context, 999,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT
                    )
                }
            }
        )
        // Device register listener
        val onDeviceRegistered: DeviceRegisteredListener = object : DeviceRegisteredListener {
            override fun onDeviceRegisteredSuccess() { // Device successfully registered on server
                StreamChat.getLogger().logI(
                    io.getstream.chat.example.BaseApplication.TAG,
                    "Device registered successfully"
                )
            }

            override fun onDeviceRegisteredError(error: ChatError) {
                StreamChat.getLogger().logE(
                    io.getstream.chat.example.BaseApplication.TAG,
                    "onDeviceRegisteredError: $errorMessage Code: $errorCode"
                )
            }
        }
        val messageListener: NotificationMessageLoadListener =
            object : NotificationMessageLoadListener {
                override fun onLoadMessageSuccess(message: Message) {
                    StreamChat.getLogger().logD(
                        io.getstream.chat.example.BaseApplication.TAG,
                        "On message loaded. Message:$message"
                    )
                }

                override fun onLoadMessageFail(messageId: String) {
                    StreamChat.getLogger().logD(
                        io.getstream.chat.example.BaseApplication.TAG,
                        "Message from notification load fails. MessageId:$messageId"
                    )
                }
            }
        var streamNotificationsManager = StreamNotificationsManager(notificationOptions, onDeviceRegistered)
        notificationsManager.setFailMessageListener(messageListener)
        notificationsManager = streamNotificationsManager
    }
}