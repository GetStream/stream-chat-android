package io.getstream.chat.android.client.attachment

import android.content.Context
import io.getstream.chat.android.client.attachment.worker.UploadAttachmentsAndroidWorker
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.client.persistance.repository.AttachmentRepository
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.utils.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.UUID

internal class AttachmentsSender(
    private val context: Context,
    private val networkType: UploadAttachmentsNetworkType,
    private val clientState: ClientState,
    private val attachmentRepository: AttachmentRepository,
    private val messageRepository: MessageRepository,
    private val scope: CoroutineScope,
) {

    private var jobsMap: Map<String, Job> = emptyMap()
    private val uploadIds = mutableMapOf<String, UUID>()

    /**
     * Uploads the attachment of this message if there is any pending attachments and return the updated message.
     *
     * @param message [Message] whose attachments are to be uploaded.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    internal suspend fun uploadAttachments(message: Message, channelType: String, channelId: String): Result<Message> {
        return if (clientState.isNetworkAvailable) {
            waitForAttachmentsToBeSent(message, channelType, channelId)
        } else {
            enqueueAttachmentUpload(message, channelType, channelId)
            Result(ChatError("Chat is offline, not sending message with id ${message.id} and text ${message.text}"))
        }
    }

    /**
     * Waits till all attachments are uploaded or either of them fails.
     *
     * @param newMessage Message whose attachments are to be uploaded.
     */
    private suspend fun waitForAttachmentsToBeSent(
        newMessage: Message,
        channelType: String,
        channelId: String,
    ): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        var allAttachmentsUploaded = false
        var messageToBeSent = newMessage

        jobsMap = jobsMap + (
            newMessage.id to scope.launch {
                attachmentRepository.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                messageToBeSent = messageRepository.selectMessage(newMessage.id) ?: newMessage.copy(
                                    attachments = attachments.toMutableList()
                                )
                                allAttachmentsUploaded = true
                                jobsMap[newMessage.id]?.cancel()
                            }
                            attachments.any { it.uploadState is Attachment.UploadState.Failed } -> {
                                jobsMap[newMessage.id]?.cancel()
                            }
                            else -> Unit
                        }
                    }
            }
            )
        enqueueAttachmentUpload(newMessage, channelType, channelId)
        jobsMap[newMessage.id]?.join()
        return if (allAttachmentsUploaded) {
            Result.success(messageToBeSent.copy(type = Message.TYPE_REGULAR))
        } else {
            Result.error(ChatError("Could not upload attachments, not sending message with id ${newMessage.id}"))
        }.also {
            uploadIds.remove(newMessage.id)
        }
    }

    /**
     * Enqueues attachment upload work.
     */
    private fun enqueueAttachmentUpload(message: Message, channelType: String, channelId: String) {
        val workId = UploadAttachmentsAndroidWorker.start(context, channelType, channelId, message.id, networkType)
        uploadIds[message.id] = workId
    }

    /**
     * Cancels all the running job.
     */
    fun cancelJobs() {
        jobsMap.values.forEach { it.cancel() }
        uploadIds.values.forEach { UploadAttachmentsAndroidWorker.stop(context, it) }
    }
}
