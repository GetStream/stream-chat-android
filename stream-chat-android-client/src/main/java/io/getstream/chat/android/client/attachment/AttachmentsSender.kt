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

package io.getstream.chat.android.client.attachment

import android.content.Context
import io.getstream.chat.android.client.attachment.worker.UploadAttachmentsAndroidWorker
import io.getstream.chat.android.client.extensions.internal.hasPendingAttachments
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.log.taggedLogger
import io.getstream.result.Result
import io.getstream.result.StreamError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Class responsible for sending attachments to the backend.
 *
 * @param context Context.
 * @param networkType [UploadAttachmentsNetworkType]
 * @param clientState [ClientState]
 * @param scope [CoroutineScope]
 */
internal class AttachmentsSender(
    private val context: Context,
    private val networkType: UploadAttachmentsNetworkType,
    private val clientState: ClientState,
    private val scope: CoroutineScope,
) {

    private var jobsMap: Map<String, Job> = emptyMap()
    private val uploadIds = mutableMapOf<String, UUID>()
    private val logger by taggedLogger("Chat:AttachmentsSender")

    internal suspend fun sendAttachments(
        message: Message,
        channelType: String,
        channelId: String,
        isRetrying: Boolean,
        repositoryFacade: RepositoryFacade,
    ): Result<Message> {
        return if (!isRetrying) {
            if (message.hasPendingAttachments()) {
                logger.d {
                    "[sendAttachments] Message ${message.id}" +
                        " has ${message.attachments.size} pending attachments"
                }
                uploadAttachments(message, channelType, channelId, repositoryFacade)
            } else {
                logger.d { "[sendAttachments] Message ${message.id} without attachments" }
                Result.Success(message)
            }
        } else {
            logger.d { "[sendAttachments] Retrying Message ${message.id}" }
            retryMessage(message, channelType, channelId, repositoryFacade)
        }
    }

    /**
     * Tries to upload attachments of this [message] without preparing.
     *
     * It is used when we have some messages already pending in database (due to any non permanent error)
     *
     * @param message [Message] to be retried.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    private suspend fun retryMessage(
        message: Message,
        channelType: String,
        channelId: String,
        repositoryFacade: RepositoryFacade,
    ): Result<Message> =
        uploadAttachments(message, channelType, channelId, repositoryFacade)

    /**
     * Uploads the attachment of this message if there is any pending attachments and return the updated message.
     *
     * @param message [Message] whose attachments are to be uploaded.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    private suspend fun uploadAttachments(
        message: Message,
        channelType: String,
        channelId: String,
        repositoryFacade: RepositoryFacade,
    ): Result<Message> {
        return if (clientState.isNetworkAvailable) {
            waitForAttachmentsToBeSent(message, channelType, channelId, repositoryFacade)
        } else {
            enqueueAttachmentUpload(message, channelType, channelId)
            logger.d { "[uploadAttachments] Chat is offline, not sending message with id ${message.id}" }
            Result.Failure(
                StreamError.GenericError(
                    "Chat is offline, not sending message with id ${message.id} and text ${message.text}",
                )
            )
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
        repositoryFacade: RepositoryFacade,
    ): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        var allAttachmentsUploaded = false
        var messageToBeSent = newMessage

        jobsMap = jobsMap + (
            newMessage.id to scope.launch {
                repositoryFacade.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                messageToBeSent = repositoryFacade.selectMessage(newMessage.id) ?: newMessage.copy(
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
            logger.d { "[waitForAttachmentsToBeSent] All attachments for message ${newMessage.id} uploaded" }
            Result.Success(messageToBeSent.copy(type = Message.TYPE_REGULAR))
        } else {
            logger.i { "[waitForAttachmentsToBeSent] Could not upload attachments for message ${newMessage.id}" }
            Result.Failure(
                StreamError.GenericError("Could not upload attachments, not sending message with id ${newMessage.id}"),
            )
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
