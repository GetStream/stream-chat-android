package io.getstream.chat.android.livedata.service.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.FirebaseMessageParser
import io.getstream.chat.android.client.notifications.FirebaseMessageParserImpl
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class OfflineSyncFirebaseMessagingService : FirebaseMessagingService() {

    private val logger = ChatLogger.get("OfflineSyncFirebaseMessagingService")

    private val syncModule by lazy { SyncProvider(this) }
    private val notificationConfig: NotificationConfig by lazy { syncModule.notificationConfigStore.get() }
    private val firebaseMessageParser: FirebaseMessageParser by lazy { FirebaseMessageParserImpl(notificationConfig) }

    override fun onNewToken(token: String) {
        if (ChatClient.isInitialized) {
            ChatClient.instance().onNewTokenReceived(token)
        } else {
            val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()
            syncConfig?.let {
                val config = it
                val user = User(id = config.userId)
                val userToken = config.userToken
                GlobalScope.launch(DispatcherProvider.IO) {
                    val client = initClient(this@OfflineSyncFirebaseMessagingService, user, userToken, config.apiKey)
                    client.onNewTokenReceived(token)
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (!ChatClient.isValidRemoteMessage(message, notificationConfig)) {
            return
        }
        createSyncNotificationChannel()
        showForegroundNotification(notificationConfig.smallIcon)

        GlobalScope.launch(DispatcherProvider.IO) {
            val cid: String = firebaseMessageParser.parse(message).let { "${it.channelType}:${it.channelId}" }
            if (ChatDomain.isInitialized && ChatClient.isInitialized) {
                logger.logD("Starting the sync")
                performSync(ChatDomain.instance(), cid, ChatClient.instance(), message)
            } else {
                val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()
                syncConfig?.let {
                    val config = it
                    val user = User(id = config.userId)
                    val token = config.userToken
                    val client = initClient(this@OfflineSyncFirebaseMessagingService, user, token, config.apiKey)
                    val domain = initDomain(user, client)
                    logger.logD("Starting the sync, config: $syncConfig")
                    performSync(domain, cid, client, message)
                }
            }
            stopForeground(true)
            stopSelf()
        }
    }

    private fun performSync(
        domain: ChatDomain,
        cid: String,
        client: ChatClient,
        message: RemoteMessage,
    ) {
        domain.useCases.replayEventsForActiveChannels(cid).enqueue {
            if (it.isSuccess) {
                logger.logD("Sync success.")
            } else {
                logger.logD("Sync failed.")
            }
            client.onMessageReceived(message)
        }
    }

    private fun showForegroundNotification(smallIcon: Int) {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(smallIcon)
            .build()
            .apply {
                startForeground(NOTIFICATION_ID, this)
            }
    }

    private fun createSyncNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).run {
                getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    private fun initDomain(user: User, client: ChatClient): ChatDomain {
        return ChatDomain.Builder(applicationContext, client, user).build()
    }

    private suspend fun initClient(
        context: Context,
        user: User,
        userToken: String,
        apiKey: String,
    ): ChatClient {
        val notificationConfig = syncModule.notificationConfigStore.get()
        val notificationHandler = ChatNotificationHandler(context, notificationConfig)

        val client = ChatClient.Builder(apiKey, context.applicationContext)
            .notifications(notificationHandler)
            .build()

        client.connectUser(user, userToken).await()

        return client
    }

    companion object {
        const val CHANNEL_ID = "notification_channel_id"
        const val CHANNEL_NAME = "Chat messages sync"
        const val NOTIFICATION_ID = 1
    }
}
