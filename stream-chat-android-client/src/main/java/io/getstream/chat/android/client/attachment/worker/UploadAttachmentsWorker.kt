/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.attachment.worker

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.attachment.AttachmentUploader
import io.getstream.chat.android.client.attachment.AttachmentsUploadStates
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.log.StreamLog
import io.getstream.log.taggedLogger
import io.getstream.result.Error
import io.getstream.result.Result
import io.getstream.result.recover
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "Chat:UploadWorker"
private const val PROGRESS_THROTTLE_MS = 200L

@InternalStreamChatApi
public class UploadAttachmentsWorker(
    private val channelType: String,
    private val channelId: String,
    private val channelStateLogic: ChannelMessagesUpdateLogic?,
    private val messageRepository: MessageRepository,
    private val chatClient: ChatClient,
    private val attachmentUploader: AttachmentUploader = AttachmentUploader(chatClient),
) {

    private val logger by taggedLogger(TAG)

    @Suppress("TooGenericExceptionCaught")
    @InternalStreamChatApi
    public suspend fun uploadAttachmentsForMessage(messageId: String): Result<Unit> {
        val message = channelStateLogic?.channelState()?.getMessageById(messageId)
            ?: messageRepository.selectMessage(messageId)

        return try {
            message?.let { sendAttachments(it) } ?: Result.Failure(
                Error.GenericError("The message with id $messageId could not be found."),
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e { "[uploadAttachmentsForMessage] #uploader; couldn't upload attachments ${e.message}" }
            message?.let { updateMessages(it) }
            Result.Failure(
                Error.ThrowableError(
                    message = "Could not upload attachments for message $messageId",
                    cause = e,
                ),
            )
        }
    }

    private suspend fun sendAttachments(message: Message): Result<Unit> {
        if (chatClient.getCurrentUser() == null) {
            logger.d { "[sendAttachments] #uploader; current user is not set. Restoring credentials" }
            if (!chatClient.containsStoredCredentials()) {
                logger.e { "[sendAttachments] #uploader; user's credentials are not available" }
                return Result.Failure(Error.GenericError("Could not set user"))
            }

            chatClient.setUserWithoutConnectingIfNeeded()
        }

        val hasPendingAttachment = message.attachments.any { attachment ->
            attachment.uploadState is Attachment.UploadState.InProgress ||
                attachment.uploadState is Attachment.UploadState.Idle
        }

        return if (!hasPendingAttachment) {
            logger.d { "[sendAttachments] #uploader; message ${message.id} doesn't have pending attachments" }
            AttachmentsUploadStates.updateMessageAttachments(message)
            Result.Success(Unit)
        } else {
            val attachments = uploadAttachments(message)
            updateMessages(message.copy(attachments = attachments))

            if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                logger.d { "[sendAttachments] #uploader; all attachments for message ${message.id} uploaded" }
                Result.Success(Unit)
            } else {
                logger.e { "[sendAttachments] #uploader; unable to upload attachments for message ${message.id}" }
                Result.Failure(Error.GenericError("Unable to upload attachments for message ${message.id}"))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun uploadAttachments(message: Message): List<Attachment> {
        val attachments = message.attachments.toMutableList()
        val attachmentsLock = Mutex()
        return try {
            coroutineScope {
                message.attachments.indices.map { index ->
                    async {
                        if (message.attachments[index].uploadState != Attachment.UploadState.Success) {
                            uploadAttachment(message, attachments, attachmentsLock, index)
                        } else {
                            logger.i {
                                "[uploadAttachments] #uploader; attachment ${message.attachments[index].uploadId}" +
                                    " for message ${message.id} already uploaded"
                            }
                        }
                    }
                }.awaitAll()
            }
            attachments
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e { "[uploadAttachments] #uploader; unable to upload attachments: ${e.message}" }
            attachments.map {
                it.copy(
                    uploadState = it.uploadState
                        .takeIf { it == Attachment.UploadState.Success }
                        ?: Attachment.UploadState.Failed(
                            Error.ThrowableError(message = "Could not upload attachments.", cause = e),
                        ),
                )
            }
        }
    }

    /**
     * Uploads the attachment at [index] and replaces it in [attachments], guarded by [lock], with the result.
     */
    private suspend fun uploadAttachment(
        message: Message,
        attachments: MutableList<Attachment>,
        lock: Mutex,
        index: Int,
    ) {
        val attachment = message.attachments[index]
        logger.d {
            "[uploadAttachments] #uploader; uploading attachment ${attachment.uploadId} for message ${message.id}"
        }
        val progressCallback = channelStateLogic?.let { logic ->
            ProgressCallbackImpl(message.id, attachment.uploadId!!, logic)
        }
        val result = attachmentUploader
            .uploadAttachment(channelType, channelId, attachment, message.id, progressCallback)
        val uploaded = result
            .recover { error -> attachment.copy(uploadState = Attachment.UploadState.Failed(error)) }
            .value
        if (result is Result.Success) {
            // Persist each upload as it completes, so a stopped worker can't lose it and upload it again.
            withContext(NonCancellable) {
                lock.withLock {
                    attachments[index] = uploaded
                    updateMessages(message.copy(attachments = attachments.toList()))
                }
            }
        } else {
            lock.withLock { attachments[index] = uploaded }
        }
    }

    private suspend fun updateMessages(message: Message) {
        val updatedMessage = message.copy(
            syncStatus = message.syncStatus
                .takeUnless {
                    message.attachments.any { attachment -> attachment.uploadState is Attachment.UploadState.Failed }
                } ?: SyncStatus.FAILED_PERMANENTLY,
        )
        channelStateLogic?.upsertMessage(updatedMessage)
        messageRepository.insertMessage(updatedMessage)
        AttachmentsUploadStates.updateMessageAttachments(updatedMessage)
    }

    private class ProgressCallbackImpl(
        private val messageId: String,
        private val uploadId: String,
        private val channelStateLogic: ChannelMessagesUpdateLogic,
    ) :
        ProgressCallback {

        private var lastProgressUpdateTime = 0L

        override fun onSuccess(url: String?) {
            StreamLog.i(TAG) { "[Progress.onSuccess] #uploader; url: $url" }
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Success)
        }

        override fun onError(error: Error) {
            StreamLog.e(TAG) { "[Progress.onError] #uploader; error: $error" }
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Failed(error))
        }

        override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
            val now = System.currentTimeMillis()
            if (now - lastProgressUpdateTime < PROGRESS_THROTTLE_MS && bytesUploaded < totalBytes) return
            lastProgressUpdateTime = now

            updateAttachmentUploadState(
                messageId,
                uploadId,
                Attachment.UploadState.InProgress(bytesUploaded, totalBytes),
            )
        }

        private fun updateAttachmentUploadState(messageId: String, uploadId: String, newState: Attachment.UploadState) {
            val message = channelStateLogic.channelState().messages.value.firstOrNull { it.id == messageId }
            if (message != null) {
                val newAttachments = message.attachments.map { attachment ->
                    if (attachment.uploadId == uploadId) {
                        attachment.copy(uploadState = newState)
                    } else {
                        attachment
                    }
                }
                val updatedMessage = message.copy(attachments = newAttachments.toMutableList())
                channelStateLogic.upsertMessage(updatedMessage)
            }
        }
    }
}
