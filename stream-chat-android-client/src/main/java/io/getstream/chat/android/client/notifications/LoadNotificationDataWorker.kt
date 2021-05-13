package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.logger.ChatLogger

internal class LoadNotificationDataWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val logger = ChatLogger.get("LoadNotificationDataWorker")

    private val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
    private val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
    private val messageId: String = inputData.getString(DATA_MESSAGE_ID)!!
    private val notificationTitle: String = inputData.getString(DATA_NOTIFICATION_TITLE)!!
    private val notificationIcon: Int = inputData.getInt(DATA_NOTIFICATION_ICON, R.drawable.stream_ic_notification)
    private val notificationChannelName: String = inputData.getString(DATA_NOTIFICATION_CHANNEL_NAME)!!

    override suspend fun doWork(): Result {
        setForeground(createForegroundInfo())

        return try {
            ChatClient.loadNotificationInfo(channelId, channelType, messageId)
            Result.success()
        } catch (exception: IllegalStateException) {
            logger.logE("Error while loading notification data: ${exception.message}")
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createForegroundNotification())
    }

    private fun createForegroundNotification(): Notification {
        createSyncNotificationChannel()
        return NotificationCompat.Builder(context, channelId)
            .setAutoCancel(true)
            .setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle)
            .build()
    }

    private fun createSyncNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(channelId, notificationChannelName, NotificationManager.IMPORTANCE_HIGH).run {
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    internal companion object {
        internal const val DATA_CHANNEL_TYPE = "DATA_CHANNEL_TYPE"
        internal const val DATA_CHANNEL_ID = "DATA_CHANNEL_ID"
        internal const val DATA_MESSAGE_ID = "DATA_MESSAGE_ID"
        internal const val DATA_NOTIFICATION_TITLE = "DATA_NOTIFICATION_TITLE"
        internal const val DATA_NOTIFICATION_ICON = "DATA_NOTIFICATION_ICON"
        internal const val DATA_NOTIFICATION_CHANNEL_NAME = "DATA_NOTIFICATION_CHANNEL_NAME"

        private const val NOTIFICATION_ID = 1
    }
}
