package io.getstream.chat.android.offline.message.attachment

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.recover
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.repository.RepositoryFacade

internal class UploadAttachmentsWorker(
    private val logic: LogicRegistry,
    private val repos: RepositoryFacade,
) {
    suspend fun uploadAttachmentsForMessage(
        channelType: String,
        channelId: String,
        messageId: String,
        chatClient: ChatClient,
    ): Result<Unit> {
        return try {
            chatClient.apply {
                if (getCurrentUser() == null) {
                    if (!chatClient.containsStoredCredentials()) {
                        return Result.error(ChatError("Could not set user"))
                    }

                    chatClient.setUserWithoutConnectingIfNeeded()
                }
            }

            val message = repos.selectMessage(messageId)

            if (message == null) {
                Result.success(Unit)
            } else {
                val hasPendingAttachment = message.attachments.any { attachment ->
                    attachment.uploadState is Attachment.UploadState.InProgress ||
                        attachment.uploadState is Attachment.UploadState.Idle
                }

                if (!hasPendingAttachment) {
                    return Result.success(Unit)
                }

                val channel = logic.channel(channelType, channelId)
                val attachments = uploadAttachments(
                    message,
                    repos,
                    channel,
                    AttachmentUploader(ChatClient.instance()),
                    channelType,
                    channelId
                )

                if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                    Result.success(Unit)
                } else {
                    Result.error(ChatError())
                }
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    private suspend fun uploadAttachments(
        message: Message,
        repos: RepositoryFacade,
        channel: ChannelLogic,
        attachmentUploader: AttachmentUploader,
        channelType: String,
        channelId: String,
    ): List<Attachment> {
        return try {
            message.attachments.map { attachment ->
                if (attachment.uploadState != Attachment.UploadState.Success) {
                    attachmentUploader.uploadAttachment(
                        channelType,
                        channelId,
                        attachment,
                        ProgressCallbackImpl(
                            message.id,
                            attachment.uploadId!!,
                            logic.channel(channelType, channelId)
                        )
                    )
                        .recover { error -> attachment.apply { uploadState = Attachment.UploadState.Failed(error) } }
                        .data()
                } else {
                    attachment
                }
            }.toMutableList()
        } catch (e: Exception) {
            message.attachments.map {
                if (it.uploadState != Attachment.UploadState.Success) {
                    it.uploadState = Attachment.UploadState.Failed(ChatError(e.message, e))
                }
                it
            }.toMutableList()
        }.also { attachments ->
            message.attachments = attachments
            // TODO refactor this place. A lot of side effects happening here.
            //  We should extract it to entity that will handle logic of uploading only.
            if (message.attachments.any { attachment -> attachment.uploadState is Attachment.UploadState.Failed }) {
                message.syncStatus = SyncStatus.FAILED_PERMANENTLY
            }
            // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
            repos.deleteChannelMessage(message)
            repos.insertMessage(message)
            channel.upsertMessage(message)
        }
    }

    internal inner class ProgressCallbackImpl(
        private val messageId: String,
        private val uploadId: String,
        private val channel: ChannelLogic,
    ) :
        ProgressCallback {
        override fun onSuccess(url: String?) {
            channel.updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Success)
        }

        override fun onError(error: ChatError) {
            channel.updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Failed(error))
        }

        override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
            channel.updateAttachmentUploadState(
                messageId,
                uploadId,
                Attachment.UploadState.InProgress(bytesUploaded, totalBytes)
            )
        }
    }
}
