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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistance.repository.MessageRepository
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.recover
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelStateLogic
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.logging.StreamLog

private const val TAG = "Chat:UploadWorker"

internal class UploadAttachmentsWorker(
    private val channelType: String,
    private val channelId: String,
    private val channelStateLogic: ChannelStateLogic,
    private val messageRepository: MessageRepository,
    private val chatClient: ChatClient,
    private val attachmentUploader: AttachmentUploader = AttachmentUploader(chatClient),
) {

    private val logger = StreamLog.getLogger(TAG)

    @Suppress("TooGenericExceptionCaught")
    suspend fun uploadAttachmentsForMessage(
        messageId: String,
    ): Result<Unit> {
        val message = messageRepository.selectMessage(messageId)

        return try {
            message?.let { sendAttachments(it) } ?: Result.error(
                ChatError("The message with id $messageId could not be found.")
            )
        } catch (e: Throwable) {
            logger.e { "[uploadAttachmentsForMessage] #uploader; couldn't upload attachments ${e.message}" }
            message?.let { updateMessages(it) }
            Result.error(e)
        }
    }

    private suspend fun sendAttachments(message: Message): Result<Unit> {
        if (chatClient.getCurrentUser() == null) {
            logger.d { "[sendAttachments] #uploader; current user is not set. Restoring credentials" }
            if (!chatClient.containsStoredCredentials()) {
                logger.e { "[sendAttachments] #uploader; user's credentials are not available" }
                return Result.error(ChatError("Could not set user"))
            }

            chatClient.setUserWithoutConnectingIfNeeded()
        }

        val hasPendingAttachment = message.attachments.any { attachment ->
            attachment.uploadState is Attachment.UploadState.InProgress ||
                attachment.uploadState is Attachment.UploadState.Idle
        }

        return if (!hasPendingAttachment) {
            logger.d { "[sendAttachments] #uploader; message ${message.id} doesn't have pending attachments" }
            Result.success(Unit)
        } else {
            val attachments = uploadAttachments(message)
            updateMessages(message)

            if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                logger.d { "[sendAttachments] #uploader; all attachments for message ${message.id} uploaded" }
                Result.success(Unit)
            } else {
                logger.e { "[sendAttachments] #uploader; unable to upload attachments for message ${message.id}" }
                Result.error(ChatError("Unable to upload attachments for message ${message.id}"))
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun uploadAttachments(
        message: Message,
    ): List<Attachment> {
        return try {
            message.attachments.map { attachment ->
                if (attachment.uploadState != Attachment.UploadState.Success) {
                    logger.d {
                        "[uploadAttachments] #uploader; uploading attachment ${attachment.uploadId} " +
                            "for message ${message.id}"
                    }
                    val progressCallback = ProgressCallbackImpl(
                        message.id,
                        attachment.uploadId!!,
                        channelStateLogic.writeChannelState()
                    )

                    attachmentUploader.uploadAttachment(channelType, channelId, attachment, progressCallback)
                        .recover { error -> attachment.apply { uploadState = Attachment.UploadState.Failed(error) } }
                        .data()
                } else {
                    logger.i {
                        "[uploadAttachments] #uploader; attachment ${attachment.uploadId}" +
                            " for message ${message.id} already uploaded"
                    }
                    attachment
                }
            }.toMutableList()
        } catch (e: Throwable) {
            logger.e { "[uploadAttachments] #uploader; unable to upload attachments: ${e.message}" }
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
        }
    }

    private suspend fun updateMessages(
        message: Message,
    ) {
        if (message.attachments.any { attachment -> attachment.uploadState is Attachment.UploadState.Failed }) {
            message.syncStatus = SyncStatus.FAILED_PERMANENTLY
        }
        channelStateLogic.upsertMessage(message)
        // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
        messageRepository.deleteChannelMessage(message)
        messageRepository.insertMessage(message)
    }

    private class ProgressCallbackImpl(
        private val messageId: String,
        private val uploadId: String,
        private val mutableState: ChannelMutableState,
    ) :
        ProgressCallback {
        override fun onSuccess(url: String?) {
            StreamLog.i(TAG) { "[Progress.onSuccess] #uploader; url: $url" }
            updateAttachmentUploadState(messageId, uploadId, Attachment.UploadState.Success)
        }

        override fun onError(error: ChatError) {
            StreamLog.e(TAG) { "[Progress.onError] #uploader; error: $error" }
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
            val message = mutableState.messageList.value.firstOrNull { it.id == messageId }
            if (message != null) {
                val newAttachments = message.attachments.map { attachment ->
                    if (attachment.uploadId == uploadId) {
                        attachment.copy(uploadState = newState)
                    } else {
                        attachment
                    }
                }
                val updatedMessage = message.copy(attachments = newAttachments.toMutableList())
                mutableState.upsertMessage(updatedMessage)
            }
        }
    }
}
