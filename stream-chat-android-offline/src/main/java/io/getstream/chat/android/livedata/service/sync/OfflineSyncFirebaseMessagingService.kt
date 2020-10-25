package io.getstream.chat.android.livedata.service.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.notifications.handler.ChatNotificationHandler
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.livedata.ChatDomain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

internal class OfflineSyncFirebaseMessagingService : FirebaseMessagingService() {

    private val logger = ChatLogger.get("OfflineSyncFirebaseMessagingService")

    private val syncModule by lazy {
        SyncProvider(this)
    }

    override fun onNewToken(token: String) {
        try {
            ChatClient.instance().onNewTokenReceived(token, this)
        } catch (e: UninitializedPropertyAccessException) {
            val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()
            val user = User(id = syncConfig.userId)
            GlobalScope.launch(Dispatchers.IO) {
                val client = initClient(
                    this@OfflineSyncFirebaseMessagingService,
                    user,
                    syncConfig.userToken,
                    syncConfig.apiKey
                )
                client.onNewTokenReceived(token, this@OfflineSyncFirebaseMessagingService)
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        createSyncNotificationChannel()
        showForegroundNotification(syncModule.notificationConfigStore.get().smallIcon)

        val data = message.data
        val channelId = data["channel_id"].toString()
        val channelType = data["channel_type"].toString()
        val cid = "$channelType:$channelId"

        GlobalScope.launch(Dispatchers.IO) {
            try {
                performSync(ChatDomain.instance(), cid)
                ChatClient.instance()
                    .onMessageReceived(message, this@OfflineSyncFirebaseMessagingService)
            } catch (e: UninitializedPropertyAccessException) {
                val syncConfig = syncModule.encryptedBackgroundSyncConfigStore.get()
                if (BackgroundSyncConfig.UNAVAILABLE != syncConfig) {
                    val user = User(id = syncConfig.userId)
                    val client = initClient(
                        this@OfflineSyncFirebaseMessagingService,
                        user,
                        syncConfig.userToken,
                        syncConfig.apiKey
                    )
                    val domain = initDomain(user, client)
                    performSync(domain, cid)
                    client.onMessageReceived(message, this@OfflineSyncFirebaseMessagingService)
                }
            } finally {
                stopForeground(true)
                stopSelf()
            }
        }
    }

    private fun performSync(domain: ChatDomain, cid: String) {
        domain.apply {
            useCases.replayEventsForActiveChannels(cid).execute()
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
        token: String,
        apiKey: String
    ): ChatClient {
        return suspendCoroutine { continuation ->
            val notificationConfig = syncModule.notificationConfigStore.get()
            val notificationHandler = ChatNotificationHandler(context, notificationConfig)

            val client = ChatClient.Builder(apiKey, context.applicationContext)
                .notifications(notificationHandler)
                .build()

            client.setUser(
                user,
                token,
                object : InitConnectionListener() {
                    override fun onSuccess(data: ConnectionData) {
                        continuation.resume(client)
                    }

                    override fun onError(error: ChatError) {
                        val cause = error.cause ?: IllegalStateException(error.message)
                        continuation.resumeWithException(cause)
                    }
                }
            )
        }
    }

    companion object {
        const val CHANNEL_ID = "notification_channel_id"
        const val CHANNEL_NAME = "Chat messages sync"
        const val NOTIFICATION_ID = 1
    }
}
