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

package io.getstream.chat.android.client.attachment.worker

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.attachment.AttachmentUploader
import io.getstream.chat.android.client.channel.ChannelMessagesUpdateLogic
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.recover
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.SyncStatus
import io.getstream.log.StreamLog

@InternalStreamChatApi
public class UploadAttachmentsWorker(
    private val channelType: String,
    private val channelId: String,
    private val channelStateLogic: ChannelMessagesUpdateLogic?,
    private val messageRepository: MessageRepository,
    private val chatClient: ChatClient,
    private val attachmentUploader: AttachmentUploader = AttachmentUploader(chatClient),
) {

    private val logger = StreamLog.getLogger("Chat:UploadAttachmentsWorker")

    @Suppress("TooGenericExceptionCaught")
    @InternalStreamChatApi
    public suspend fun uploadAttachmentsForMessage(messageId: String): Result<Unit> {
        val message = channelStateLogic?.listenForChannelState()?.getMessageById(messageId)
            ?: messageRepository.selectMessage(messageId)

        return try {
            message?.let { sendAttachments(it) } ?: Result.Failure(
                ChatError.GenericError("The message with id $messageId could not be found.")
            )
        } catch (e: Exception) {
            logger.i { "[uploadAttachmentsForMessage] Couldn't upload attachments ${e.message}" }
            message?.let { updateMessages(it) }
            Result.Failure(
                ChatError.ThrowableError(
                    message = "Could not upload attachments for message $messageId",
                    cause = e,
                ),
            )
        }
    }

    private suspend fun sendAttachments(message: Message): Result<Unit> {
        if (chatClient.getCurrentUser() == null) {
            logger.d { "[sendAttachments] Current user is not set. Restoring credentials" }
            if (!chatClient.containsStoredCredentials()) {
                logger.d { "[sendAttachments] User's credentials are not available" }
                return Result.Failure(ChatError.GenericError("Could not set user"))
            }

            chatClient.setUserWithoutConnectingIfNeeded()
        }

        val hasPendingAttachment = message.attachments.any { attachment ->
            attachment.uploadState is Attachment.UploadState.InProgress ||
                attachment.uploadState is Attachment.UploadState.Idle
        }

        return if (!hasPendingAttachment) {
            logger.d { "[sendAttachments] Message ${message.id} doesn't have pending attachments" }
            Result.Success(Unit)
        } else {
            val attachments = uploadAttachments(message)
            updateMessages(message)

            if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                logger.d { "[sendAttachments] All attachments for message ${message.id} uploaded" }
                Result.Success(Unit)
            } else {
                logger.i { "[sendAttachments] Unable to upload attachments for message ${message.id}" }
                Result.Failure(ChatError.GenericError("Unable to upload attachments for message ${message.id}"))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun uploadAttachments(message: Message): List<Attachment> {
        return try {
            message.attachments.map { attachment ->
                if (attachment.uploadState != Attachment.UploadState.Success) {
                    logger.d {
                        "[uploadAttachments] Uploading attachment ${attachment.uploadId} " +
                            "for message ${message.id}"
                    }
                    val progressCallback = channelStateLogic?.let { logic ->
                        ProgressCallbackImpl(
                            message.id,
                            attachment.uploadId!!,
                            logic
                        )
                    }

                    attachmentUploader.uploadAttachment(channelType, channelId, attachment, progressCallback)
                        .recover { error -> attachment.apply { uploadState = Attachment.UploadState.Failed(error) } }
                        .value
                } else {
                    logger.i {
                        "[uploadAttachments] Attachment ${attachment.uploadId}" +
                            " for message ${message.id} already uploaded"
                    }
                    attachment
                }
            }.toMutableList()
        } catch (e: Exception) {
            logger.i { "[uploadAttachments] Unable to upload attachments: ${e.message}" }
            message.attachments.map {
                if (it.uploadState != Attachment.UploadState.Success) {
                    it.uploadState = Attachment.UploadState.Failed(
                        ChatError.ThrowableError(message = "Could not upload attachments.", cause = e),
                    )
                }
                it
            }.toMutableList()
        }.also { attachments ->
            message.attachments = attachments
        }
    }

    private suspend fun updateMessages(message: Message) {
        if (message.attachments.any { attachment -> attachment.uploadState is Attachment.UploadState.Failed }) {
            message.syncStatus = SyncStatus.FAILED_PERMANENTLY
        }
        channelStateLogic?.upsertMessage(message)
        // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
        messageRepository.deleteChannelMessage(message)
        messageRepository.insertMessage(message)
    }

    private class ProgressCallbackImpl(
        private val messageId: String,
        private val uploadId: String,
        private val channelStateLogic: ChannelMessagesUpdateLogic,
    ) :
        ProgressCallback {
        override fun onSuccess(url: String?) {
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Success)
        }

        override fun onError(error: ChatError) {
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Failed(error))
        }

        override fun onProgress(bytesUploaded: Long, totalBytes: Long) {
            updateAttachmentUploadState(
                messageId,
                uploadId,
                Attachment.UploadState.InProgress(bytesUploaded, totalBytes)
            )
        }

        private fun updateAttachmentUploadState(messageId: String, uploadId: String, newState: Attachment.UploadState) {
            val message = channelStateLogic.listenForChannelState().messages.value.firstOrNull { it.id == messageId }
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
