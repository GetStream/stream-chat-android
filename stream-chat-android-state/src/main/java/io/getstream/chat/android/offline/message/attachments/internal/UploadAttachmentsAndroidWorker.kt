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

package io.getstream.chat.android.offline.message.attachments.internal

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.model.message.attachments.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.logging.StreamLog
import java.util.UUID

internal class UploadAttachmentsAndroidWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val messageId = inputData.getString(DATA_MESSAGE_ID)!!

        val logger = StreamLog.getLogger(TAG)
        val chatClient = ChatClient.instance()
        val repositoryFacade = chatClient.repositoryFacade

        return UploadAttachmentsWorker(
            channelType = channelType,
            channelId = channelId,
            channelStateLogic = LogicRegistry.get().channelStateLogic(channelType, channelId),
            messageRepository = repositoryFacade,
            chatClient = chatClient
        ).uploadAttachmentsForMessage(
            messageId
        ).let { result ->
            if (result.isSuccess) {
                logger.d { "[doWork] #uploader; attachments uploaded successfully" }
                Result.success()
            } else {
                logger.i { "[doWork] #uploader; error while uploading attachments: ${result.error()}" }
                val message = result.error().message ?: "Error while uploading attachments"
                Result.failure(Data.Builder().putAll(mapOf(ERROR_KEY to message)).build())
            }
        }
    }

    internal companion object {
        private const val TAG = "Chat:SystemUploadWorker"
        private const val DATA_MESSAGE_ID = "message_id"
        private const val DATA_CHANNEL_TYPE = "channel_type"
        private const val DATA_CHANNEL_ID = "channel_id"
        private const val ERROR_KEY = "error"

        internal fun start(
            context: Context,
            channelType: String,
            channelId: String,
            messageId: String,
            networkType: UploadAttachmentsNetworkType,
        ): UUID {
            val uploadAttachmentsWorkRequest = OneTimeWorkRequestBuilder<UploadAttachmentsAndroidWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(networkType.toNetworkType()).build())
                .setInputData(
                    workDataOf(
                        DATA_CHANNEL_ID to channelId,
                        DATA_CHANNEL_TYPE to channelType,
                        DATA_MESSAGE_ID to messageId,
                    )
                )
                .build()

            StreamLog.getLogger(TAG)
                .d { "[start] #uploader; enqueueing attachments upload work for $messageId" }

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$channelId$messageId",
                ExistingWorkPolicy.KEEP,
                uploadAttachmentsWorkRequest
            )
            return uploadAttachmentsWorkRequest.id
        }

        /**
         * Stops the ongoing work if there is any.
         *
         * @param context Context of the application.
         * @param workId UUID of the enqueued work.
         */
        internal fun stop(context: Context, workId: UUID) {
            StreamLog.getLogger(TAG)
                .d { "[stop] #uploader; upload attachments work cancelled" }
            WorkManager.getInstance(context).cancelWorkById(workId)
        }
    }
}
