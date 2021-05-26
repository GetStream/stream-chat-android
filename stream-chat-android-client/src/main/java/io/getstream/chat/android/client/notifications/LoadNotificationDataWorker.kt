package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.R
import io.getstream.chat.android.client.logger.ChatLogger

internal class LoadNotificationDataWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val logger = ChatLogger.get("LoadNotificationDataWorker")

    override suspend fun doWork(): Result {
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val messageId: String = inputData.getString(DATA_MESSAGE_ID)!!
        val notificationTitle: String = inputData.getString(DATA_NOTIFICATION_TITLE)!!
        val notificationIcon: Int = inputData.getInt(DATA_NOTIFICATION_ICON, R.drawable.stream_ic_notification)
        val notificationChannelName: String = inputData.getString(DATA_NOTIFICATION_CHANNEL_NAME)!!

        setForeground(
            createForegroundInfo(
                notificationChannelId = LOAD_NOTIFICATION_DATA_CHANNEL_ID,
                notificationChannelName = notificationChannelName,
                notificationIcon = notificationIcon,
                notificationTitle = notificationTitle,
            )
        )

        return try {
            ChatClient.displayNotificationWithData(
                channelId = channelId,
                channelType = channelType,
                messageId = messageId,
            )
            Result.success()
        } catch (exception: IllegalStateException) {
            logger.logE("Error while loading notification data: ${exception.message}")
            Result.failure()
        }
    }

    private fun createForegroundInfo(
        notificationChannelId: String,
        notificationIcon: Int,
        notificationTitle: String,
        notificationChannelName: String,
    ): ForegroundInfo {
        return ForegroundInfo(
            NOTIFICATION_ID,
            createForegroundNotification(
                notificationChannelId = notificationChannelId,
                notificationChannelName = notificationChannelName,
                notificationIcon = notificationIcon,
                notificationTitle = notificationTitle,
            ),
        )
    }

    private fun createForegroundNotification(
        notificationChannelId: String,
        notificationIcon: Int,
        notificationTitle: String,
        notificationChannelName: String,
    ): Notification {
        createSyncNotificationChannel(
            notificationChannelId = notificationChannelId,
            notificationChannelName = notificationChannelName,
        )
        return NotificationCompat.Builder(context, notificationChannelId)
            .setAutoCancel(true)
            .setSmallIcon(notificationIcon)
            .setContentTitle(notificationTitle)
            .build()
    }

    private fun createSyncNotificationChannel(
        notificationChannelId: String,
        notificationChannelName: String,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                NotificationManager.IMPORTANCE_HIGH,
            ).run {
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    internal companion object {
        private const val DATA_CHANNEL_TYPE = "DATA_CHANNEL_TYPE"
        private const val DATA_CHANNEL_ID = "DATA_CHANNEL_ID"
        private const val DATA_MESSAGE_ID = "DATA_MESSAGE_ID"
        private const val DATA_NOTIFICATION_TITLE = "DATA_NOTIFICATION_TITLE"
        private const val DATA_NOTIFICATION_ICON = "DATA_NOTIFICATION_ICON"
        private const val DATA_NOTIFICATION_CHANNEL_NAME = "DATA_NOTIFICATION_CHANNEL_NAME"

        private const val NOTIFICATION_ID = 1
        private const val LOAD_NOTIFICATION_DATA_CHANNEL_ID = "load_notification_data_channel_id"
        private const val LOAD_NOTIFICATION_DATA_WORK_NAME = "LOAD_NOTIFICATION_DATA_WORK_NAME"

        fun start(
            context: Context,
            channelId: String,
            channelType: String,
            messageId: String,
            notificationChannelName: String,
            notificationIcon: Int,
            notificationTitle: String,
        ) {
            val syncMessagesWork = OneTimeWorkRequestBuilder<LoadNotificationDataWorker>()
                .setInputData(
                    workDataOf(
                        DATA_CHANNEL_ID to channelId,
                        DATA_CHANNEL_TYPE to channelType,
                        DATA_MESSAGE_ID to messageId,
                        DATA_NOTIFICATION_CHANNEL_NAME to notificationChannelName,
                        DATA_NOTIFICATION_ICON to notificationIcon,
                        DATA_NOTIFICATION_TITLE to notificationTitle,
                    )
                )
                .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    LOAD_NOTIFICATION_DATA_WORK_NAME,
                    ExistingWorkPolicy.APPEND_OR_REPLACE,
                    syncMessagesWork,
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(LOAD_NOTIFICATION_DATA_WORK_NAME)
        }
    }
}
