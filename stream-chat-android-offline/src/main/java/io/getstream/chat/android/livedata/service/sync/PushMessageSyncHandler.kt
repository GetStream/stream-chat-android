package io.getstream.chat.android.livedata.service.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import androidx.annotation.WorkerThread
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.FirebaseMessageParser
import io.getstream.chat.android.client.notifications.FirebaseMessageParserImpl
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.livedata.ChatDomain

/**
 * A class which can be used in [com.google.firebase.messaging.FirebaseMessagingService] in case the app uses
 * multiple push notifications backend services. It's responsible for registering the device's Firebase token on Stream
 * backend, executing data sync, and showing notification about the new messages obtained during the sync.
 *
 * If the app uses Stream service as its only push notifications provider, the [OfflineSyncFirebaseMessagingService]
 * class can be used as the  out-of-the box implementation instead of [PushMessageSyncHandler] methods.
 */
public class PushMessageSyncHandler(private val service: Service) {

    private val logger = ChatLogger.get("PushMessageSyncHandler")

    private val syncModule by lazy { SyncProvider(service) }
    private val notificationConfig: NotificationConfig by lazy { syncModule.notificationConfigStore.get() }
    private val firebaseMessageParser: FirebaseMessageParser by lazy { FirebaseMessageParserImpl(notificationConfig) }

    /**
     * Should be called from [FirebaseMessagingService.onNewToken] function.
     * It registers the Firebase token at Stream backend service. It's required by the backend to send
     * push notifications via Firebase.
     *
     * @param token the token delivered by Firebase service to [com.google.firebase.messaging.FirebaseMessagingService]
     * registered at your app.
     */
    public fun onNewToken(token: String) {
        if (ChatClient.isInitialized) {
            ChatClient.instance().onNewTokenReceived(token)
        } else {
            val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()
            syncConfig?.let {
                val config = it
                val user = User(id = config.userId)
                val userToken = config.userToken
                val client = initClient(service, user, userToken, config.apiKey)
                client.onNewTokenReceived(token)
            }
        }
    }

    /**
     * This method should be called from [FirebaseMessagingService.onMessageReceived] function. It parses the [message],
     * and starts the sync operation (synchronously), proceeded with notification displaying.
     *
     * In order to perform the sync, the service is switched into foreground, and the foreground notification is
     * displayed to inform about the foreground execution. Afterwards, the service switches back to background execution
     * and a new notification is displayed informing about the new messages / events obtained during the sync.
     *
     * In case the [message] was not delivered from the Stream push notification provider no operation is executed.
     *
     * @param message the [RemoteMessage] delivered to [com.google.firebase.messaging.FirebaseMessagingService]
     * registered at your app.
     */
    @WorkerThread
    public fun onMessageReceived(message: RemoteMessage) {
        if (!isStreamMessage(message)) {
            return
        }
        createSyncNotificationChannel()
        startForegroundExecution(notificationConfig.smallIcon)

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
                val client = initClient(service, user, token, config.apiKey)
                val domain = initDomain(user, client)

                logger.logD("Starting the sync, config: $syncConfig")

                performSync(domain, cid, client, message)
            }
        }
        service.stopForeground(true)
    }

    /**
     * Checks if the cloud message is sent from the Stream backend service.
     *
     * @param message instance of [RemoteMessage] delivered at [FirebaseMessagingService]
     *
     * @return true if the message is sent from Stream backend, false otherwise.
     */
    public fun isStreamMessage(message: RemoteMessage): Boolean {
        return ChatClient.isValidRemoteMessage(message, notificationConfig)
    }

    private fun performSync(
        domain: ChatDomain,
        cid: String,
        client: ChatClient,
        message: RemoteMessage,
    ) {
        val result = domain.replayEventsForActiveChannels(cid).execute()
        if (result.isSuccess) {
            logger.logD("Sync success.")
        } else {
            logger.logD("Sync failed.")
        }
        client.onMessageReceived(message)
    }

    private fun startForegroundExecution(smallIcon: Int) {
        NotificationCompat.Builder(service, CHANNEL_ID)
            .setAutoCancel(true)
            .setSmallIcon(smallIcon)
            .build()
            .apply {
                service.startForeground(NOTIFICATION_ID, this)
            }
    }

    private fun createSyncNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).run {
                this.importance = NotificationManager.IMPORTANCE_LOW
                service.getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    private fun initDomain(user: User, client: ChatClient): ChatDomain {
        return ChatDomain.Builder(service, client).build().apply {
            currentUser = user
        }
    }

    private fun initClient(
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

        client.setUserWithoutConnecting(user, userToken)

        return client
    }

    private companion object {
        private const val CHANNEL_ID = "notification_channel_id"
        private const val CHANNEL_NAME = "Chat messages sync"
        private const val NOTIFICATION_ID = 1
    }
}
