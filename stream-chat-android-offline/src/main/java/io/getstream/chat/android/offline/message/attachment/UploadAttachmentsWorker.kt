package io.getstream.chat.android.offline.message.attachment

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl

internal class UploadAttachmentsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val messageId = inputData.getString(DATA_MESSAGE_ID)!!

        return try {
            val domainImpl = (ChatDomain.instance() as ChatDomainImpl)
            val message = domainImpl.repos.selectMessage(messageId)!!
            val attachments = domainImpl.channel(channelType, channelId)
                .uploadAttachments(message)

            if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                domainImpl.markMessageAttachmentSyncStatus(message, SyncStatus.COMPLETED)
                Result.success()
            } else {
                domainImpl.markMessageAttachmentSyncStatus(message, SyncStatus.FAILED_PERMANENTLY)
                Result.failure()
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun ChatDomainImpl.markMessageAttachmentSyncStatus(
        message: Message,
        syncStatus: SyncStatus,
    ) = repos.insertMessage(message.copy(attachmentsSyncStatus = syncStatus))

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
            val uploadAttachmentsWorRequest = OneTimeWorkRequestBuilder<UploadAttachmentsWorker>()
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
