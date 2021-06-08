package io.getstream.chat.android.offline.message.attachment

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.logger.ChatLogger

internal class UploadAttachmentsAndroidWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    private val logger = ChatLogger.get("UploadAttachmentsAndroidWorker")

    override suspend fun doWork(): Result {
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val messageId = inputData.getString(DATA_MESSAGE_ID)!!

        logger.logW("Start sending attachments for message $messageId")

        return UploadAttachmentsWorker()
            .uploadAttachmentsForMessage(channelType, channelId, messageId)
            .run { if (isSuccess) Result.success() else Result.failure() }
            .also {
                logger.logW("Finish sending attachments for message $messageId. Is successful? ${it is Result.Success}")
            }
    }

    companion object {
        private const val DATA_MESSAGE_ID = "message_id"
        private const val DATA_CHANNEL_TYPE = "channel_type"
        private const val DATA_CHANNEL_ID = "channel_id"

        fun start(
            context: Context,
            channelType: String,
            channelId: String,
            messageId: String,
        ) {
            val uploadAttachmentsWorRequest = OneTimeWorkRequestBuilder<UploadAttachmentsAndroidWorker>()
                .setInputData(
                    workDataOf(
                        DATA_CHANNEL_ID to channelId,
                        DATA_CHANNEL_TYPE to channelType,
                        DATA_MESSAGE_ID to messageId,
                    )
                )
                .build()

            WorkManager.getInstance(context)
                .enqueue(uploadAttachmentsWorRequest)
        }
    }
}
