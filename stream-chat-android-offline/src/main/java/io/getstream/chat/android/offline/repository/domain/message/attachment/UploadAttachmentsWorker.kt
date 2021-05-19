package io.getstream.chat.android.offline.repository.domain.message.attachment

import android.content.Context
import android.webkit.MimeTypeMap
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.uploader.ProgressTracker
import io.getstream.chat.android.client.uploader.ProgressTrackerFactory
import io.getstream.chat.android.client.uploader.StreamCdnImageMimeTypes
import io.getstream.chat.android.client.uploader.toProgressCallback
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.extensions.isPermanent
import io.getstream.chat.android.offline.repository.RepositoryFacade
import java.io.File
import io.getstream.chat.android.client.utils.Result as CallResult

internal class UploadAttachmentsWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    val repository: RepositoryFacade
        get() {
            return (ChatDomain.instance() as ChatDomainImpl).repos
        }

    override suspend fun doWork(): Result {
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val messageId = inputData.getString(DATA_MESSAGE_ID)!!

        val attachmentEntities: List<AttachmentToUploadEntity> = repository
            .selectAttachmentsToUploadForMessageId(messageId)
            .filter { it.syncStatus == SyncStatus.SYNC_NEEDED }

        if (runAttemptCount > RETRY_COUNT) {
            cleanup(attachmentEntities)
            return Result.failure()
        }

        return try {
            var batchUploaded = true

            attachmentEntities.forEach { entity ->
                repository.updateAttachmentsToUpload(entity.copy(syncStatus = SyncStatus.IN_PROGRESS))

                val result = uploadAttachment(channelType, channelId, entity)
                if (result.isSuccess) {
                    val augmentedEntity = result.data()
                    repository.updateAttachmentsToUpload(augmentedEntity.copy(syncStatus = SyncStatus.COMPLETED))
                    augmentedEntity.obtainProgressTracker().setComplete(true)
                } else {
                    if (result.error().isPermanent()) {
                        repository.updateAttachmentsToUpload(entity.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY))
                        entity.obtainProgressTracker().setComplete(false)
                    } else {
                        repository.updateAttachmentsToUpload(entity.copy(syncStatus = SyncStatus.SYNC_NEEDED))
                    }
                    batchUploaded = false
                }
            }

            if (batchUploaded) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            cleanup(attachmentEntities)
            Result.failure()
        }
    }

    private suspend fun cleanup(attachmentEntities: List<AttachmentToUploadEntity>) {
        attachmentEntities.forEach { entity ->
            repository.updateAttachmentsToUpload(entity.copy(syncStatus = SyncStatus.FAILED_PERMANENTLY))
        }
    }

    private suspend fun uploadAttachment(
        channelType: String,
        channelId: String,
        attachmentEntity: AttachmentToUploadEntity,
    ): CallResult<AttachmentToUploadEntity> {
        val uploadFile = File(attachmentEntity.uploadFilePath)
        val uploadMimeType = getUploadMimeType(uploadFile)

        val result = uploadAttachment(
            channelType = channelType,
            channelId = channelId,
            file = uploadFile,
            progressTracker = attachmentEntity.obtainProgressTracker(),
            uploadMimeType = uploadMimeType
        )

        return if (result.isSuccess) {
            val url = result.data()
            val augmentedAttachmentEntity = attachmentEntity.copy(
                name = uploadFile.name,
                fileSize = uploadFile.length().toInt(),
                mimeType = uploadMimeType.toString(),
                url = url,
                uploadState = Attachment.UploadState.Success.toEntity(),
                type = uploadMimeType.toString(),
                imageUrl = if (uploadMimeType == UploadMimeType.IMAGE) url else null,
                assetUrl = if (uploadMimeType != UploadMimeType.IMAGE) url else null,
            )
            CallResult(augmentedAttachmentEntity)
        } else {
            CallResult(result.error())
        }
    }

    private suspend fun uploadAttachment(
        channelType: String,
        channelId: String,
        file: File,
        progressTracker: ProgressTracker,
        uploadMimeType: UploadMimeType,
    ): CallResult<String> {
        return if (uploadMimeType == UploadMimeType.IMAGE) {
            ChatClient.instance()
                .channel(channelType, channelId)
                .sendFile(file, progressTracker.toProgressCallback())
                .await()
        } else {
            ChatClient.instance()
                .channel(channelType, channelId)
                .sendFile(file, progressTracker.toProgressCallback())
                .await()
        }
    }

    private fun AttachmentToUploadEntity.obtainProgressTracker(): ProgressTracker {
        return ProgressTrackerFactory.getOrCreate(uploadId).apply {
            maxValue = File(uploadFilePath).length()
        }
    }

    private fun getUploadMimeType(file: File): UploadMimeType {
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension)

        val isImage = StreamCdnImageMimeTypes.isImageMimeTypeSupported(mimeType)
        val isVideo = mimeType?.contains(UploadMimeType.VIDEO.toString()) ?: false

        return when {
            isImage -> UploadMimeType.IMAGE
            isVideo -> UploadMimeType.VIDEO
            else -> UploadMimeType.FILE
        }
    }

    internal enum class UploadMimeType(private val value: String) {
        VIDEO("video"),
        IMAGE("image"),
        FILE("file");

        override fun toString(): String {
            return value
        }
    }

    companion object {
        private const val RETRY_COUNT = 3
        private const val DATA_MESSAGE_ID = "message_id"
        private const val DATA_CHANNEL_TYPE = "channel_type"
        private const val DATA_CHANNEL_ID = "channel_id"

        fun start(context: Context, channelType: String, channelId: String, messageId: String) {
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
