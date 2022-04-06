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
 
package io.getstream.chat.android.client.uploader.worker

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.manager.ChannelStateManager
import io.getstream.chat.android.client.channel.manager.ChannelsManager
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.extensions.users
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.persistence.MessageRepository
import io.getstream.chat.android.client.persistence.UserRepository
import io.getstream.chat.android.client.uploader.AttachmentUploader
import io.getstream.chat.android.client.utils.ProgressCallback
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.recover

internal class UploadAttachmentsWorker(
    private val channelsManager: ChannelsManager,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatClient: ChatClient,
    private val attachmentUploader: AttachmentUploader = AttachmentUploader(chatClient),
) {

    suspend fun uploadAttachmentsForMessage(
        channelType: String,
        channelId: String,
        messageId: String,
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

            val message = messageRepository.selectMessage(messageId)

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

                val attachments = uploadAttachments(
                    message,
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
                            channelsManager.channel(channelType, channelId)
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
            if (message.attachments.any { attachment -> attachment.uploadState is Attachment.UploadState.Failed }) {
                message.syncStatus = SyncStatus.FAILED_PERMANENTLY
            }
            // RepositoryFacade::insertMessage is implemented as upsert, therefore we need to delete the message first
            messageRepository.deleteChannelMessage(message)

            userRepository.insertUsers(message.users())
            messageRepository.insertMessage(message)

            channelsManager.channel(channelType, channelId).upsertMessage(message)
        }
    }

    private class ProgressCallbackImpl(
        private val messageId: String,
        private val uploadId: String,
        private val channel: ChannelStateManager,
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
