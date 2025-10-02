/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SHORT_SERVICE
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
import io.getstream.chat.android.client.api.models.QueryChannelRequest
import io.getstream.chat.android.client.notifications.handler.ChatNotification
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import io.getstream.result.call.zipWith

internal class LoadNotificationDataWorker(
    private val context: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(context, workerParams) {

    private val logger by taggedLogger(TAG)

    override suspend fun doWork(): Result {
        val type = inputData.getString(DATA_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val messageId: String = inputData.getString(DATA_MESSAGE_ID)!!

        setForeground(createForegroundInfo())

        logger.d { "[doWork] cid: $channelType:$channelId, messageId: $messageId" }

        return try {
            val client: ChatClient = ChatClient.instance()
            val getMessage = client.getMessage(messageId)
            val getChannel = client.queryChannel(
                channelType,
                channelId,
                QueryChannelRequest().apply {
                    isNotificationUpdate = true
                },
            )

            val result = getChannel.zipWith(getMessage).await()
            when (result) {
                is io.getstream.result.Result.Success -> {
                    val (channel, message) = result.value
                    val messageParentId = message.parentId

                    if (messageParentId != null) {
                        logger.v { "[doWork] fetching thread parent message." }
                        client.getMessage(messageParentId).await()
                    }
                    ChatNotification.create(type, inputData.keyValueMap, channel, message)
                        ?.let(ChatClient::displayNotification)
                    logger.v { "[doWork] completed" }
                    Result.success()
                }
                is io.getstream.result.Result.Failure -> {
                    logger.e { "[doWork] failed: ${result.value}" }
                    Result.failure()
                }
            }
        } catch (exception: IllegalStateException) {
            logger.e { "[doWork] failed unexpectedly: ${exception.message}" }
            Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val foregroundNotification = createForegroundNotification(
            notificationChannelId = context.getString(R.string.stream_chat_other_notifications_channel_id),
            notificationChannelName = context.getString(R.string.stream_chat_other_notifications_channel_name),
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ForegroundInfo(
                NOTIFICATION_ID,
                foregroundNotification,
                FOREGROUND_SERVICE_TYPE_SHORT_SERVICE,
            )
        } else {
            ForegroundInfo(
                NOTIFICATION_ID,
                foregroundNotification,
            )
        }
    }

    private fun createForegroundNotification(
        notificationChannelId: String,
        notificationChannelName: String,
    ): Notification {
        createSyncNotificationChannel(notificationChannelId, notificationChannelName)
        return NotificationCompat.Builder(context, notificationChannelId)
            .setAutoCancel(true)
            .setSmallIcon(R.drawable.stream_ic_notification)
            .setContentTitle(context.getString(R.string.stream_chat_load_notification_data_title))
            .setPriority(NotificationCompat.PRIORITY_LOW)
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
                NotificationManager.IMPORTANCE_LOW,
            ).run {
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(this)
            }
        }
    }

    internal companion object {
        private const val TAG = "Chat:Notifications-Loader"
        private const val DATA_TYPE = "DATA_TYPE"
        private const val DATA_CHANNEL_TYPE = "DATA_CHANNEL_TYPE"
        private const val DATA_CHANNEL_ID = "DATA_CHANNEL_ID"
        private const val DATA_MESSAGE_ID = "DATA_MESSAGE_ID"

        private const val NOTIFICATION_ID = 1
        private const val LOAD_NOTIFICATION_DATA_WORK_NAME = "LOAD_NOTIFICATION_DATA_WORK_NAME"

        @Suppress("LongParameterList", "SpreadOperator")
        fun start(
            context: Context,
            type: String,
            channelId: String,
            channelType: String,
            messageId: String,
            payload: Map<String, Any?>,
        ) {
            StreamLog.d(TAG) { "/start/ cid: $channelType:$channelId, messageId: $messageId" }
            val dataPairs = listOf<Pair<String, Any?>>(
                DATA_TYPE to type,
                DATA_CHANNEL_ID to channelId,
                DATA_CHANNEL_TYPE to channelType,
                DATA_MESSAGE_ID to messageId,
            ) + payload.map { entry -> entry.key to entry.value }
            val syncMessagesWork = OneTimeWorkRequestBuilder<LoadNotificationDataWorker>()
                .setInputData(workDataOf(*dataPairs.toTypedArray()))
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
            StreamLog.d(TAG) { "/cancel/ no args" }
            WorkManager.getInstance(context).cancelUniqueWork(LOAD_NOTIFICATION_DATA_WORK_NAME)
        }
    }
}
