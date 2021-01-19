package io.getstream.chat.android.client.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class ChatFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = ChatLogger.get("ChatFirebaseMessagingService")
    private val defaultNotificationConfig = NotificationConfig()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        logger.logD("onMessageReceived(): $remoteMessage")
        if (!ChatClient.isValidRemoteMessage(remoteMessage, defaultNotificationConfig)) {
            return
        }
        createSyncNotificationChannel()
        showForegroundNotification(defaultNotificationConfig.smallIcon)
        GlobalScope.launch(DispatcherProvider.IO) {
            if (ChatClient.isInitialized) {
                ChatClient.instance().onMessageReceived(remoteMessage)
            }
            stopForeground(true)
            stopSelf()
        }
    }

    override fun onNewToken(token: String) {
        if (ChatClient.isInitialized) {
            ChatClient.instance().onNewTokenReceived(token)
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

    private companion object {
        private const val CHANNEL_ID = "notification_channel_id"
        private const val CHANNEL_NAME = "Chat messages sync"
        private const val NOTIFICATION_ID = 1
    }
}
