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
import io.getstream.chat.android.offline.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState
import io.getstream.chat.android.offline.plugin.state.channel.internal.toMutableState

internal class UploadAttachmentsWorker(
    private val channelType: String,
    private val channelId: String,
    private val channelLogic: ChannelLogic,
    private val channelState: ChannelState,
    private val messageRepository: MessageRepository,
    private val chatClient: ChatClient,
    private val attachmentUploader: AttachmentUploader = AttachmentUploader(chatClient),
) {

    suspend fun uploadAttachmentsForMessage(
        messageId: String,
    ): Result<Unit> {
        val message = messageRepository.selectMessage(messageId)

        return try {
            chatClient.apply {
                if (getCurrentUser() == null) {
                    if (!chatClient.containsStoredCredentials()) {
                        return Result.error(ChatError("Could not set user"))
                    }

                    chatClient.setUserWithoutConnectingIfNeeded()
                }
            }

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

                val attachments = uploadAttachments(message)

                updateMessages(message)

                if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                    Result.success(Unit)
                } else {
                    Result.error(ChatError())
                }
            }
        } catch (e: Exception) {
            message?.let { updateMessages(it) }
            Result.error(e)
        }
    }

    private suspend fun uploadAttachments(
        message: Message,
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
                            channelState.toMutableState()
                        )
                    )
                        .recover { error -> attachment.apply { uploadState = Attachment.UploadState.Failed(error) } }
                        .data()
                } else {
                    attachment
                }
            }.toMutableList()
        } catch (e: Exception) {
            e.printStackTrace()
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
        channelLogic.upsertMessage(message)
        // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
        messageRepository.deleteChannelMessage(message)
        messageRepository.insertMessage(message)
    }

    private class ProgressCallbackImpl(
        private val messageId: String,
        private val uploadId: String,
        private val mutableState: ChannelMutableState
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
                val newMessages =
                    mutableState.messageList.value.associateBy(Message::id) + (updatedMessage.id to updatedMessage)
                mutableState._messages.value = newMessages
            }
        }
    }
}
