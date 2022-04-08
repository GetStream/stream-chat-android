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

package io.getstream.chat.android.offline.interceptor.internal

import android.content.Context
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.extensions.hasPendingAttachments
import io.getstream.chat.android.client.extensions.populateMentions
import io.getstream.chat.android.offline.message.attachments.internal.generateUploadId
import io.getstream.chat.android.client.uploader.attachment.network.UploadAttachmentsNetworkType
import io.getstream.chat.android.client.uploader.attachment.worker.UploadAttachmentsAndroidWorker
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import io.getstream.chat.android.offline.utils.internal.getMessageType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * Implementation of [SendMessageInterceptor] that upload attachments, update original message
 * with new attachments and return updated message.
 */
internal class SendMessageInterceptorImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
    private val scope: CoroutineScope,
    private val networkType: UploadAttachmentsNetworkType,
) : SendMessageInterceptor {

    private var jobsMap: Map<String, Job> = emptyMap()

    private val logger = ChatLogger.get("MessageSendingService")

    override suspend fun interceptMessage(
        channelType: String,
        channelId: String,
        message: Message,
        isRetrying: Boolean,
    ): Result<Message> {
        val channel = logic.channel(channelType, channelId)
        message.populateMentions(channel.toChannel())

        if (message.replyMessageId != null) {
            channel.replyMessage(null)
        }

        return if (!isRetrying) {
            prepareNewMessageWithAttachments(message, channelType, channelId)
        } else {
            retryMessage(message, channelType, channelId)
        }
    }

    /**
     * Prepares message and upload its attachments if it has any.
     *
     * @param message [Message] to be sent.
     *
     * @return [Result] with a prepared message.
     */
    private suspend fun prepareNewMessageWithAttachments(
        message: Message,
        channelType: String,
        channelId: String,
    ): Result<Message> {
        val preparedMessage = prepareNewMessage(message, channelType, channelId)
        return uploadAttachments(preparedMessage, channelType, channelId)
    }

    /**
     * Prepares the message and its attachments but doesn't upload attachments.
     *
     * Following steps are required to initialize message properly before sending the message to the backend API:
     * 1. Message id is generated if the message doesn't have id.
     * 2. Message cid is updated if the message doesn't have cid.
     * 3. Message user is set to the current user.
     * 4. Attachments are prepared with upload state.
     * 5. Message timestamp and sync status is set.
     *
     * Then this message is inserted in database (Optimistic UI update) and final message is returned.
     */
    private suspend fun prepareNewMessage(message: Message, channelType: String, channelId: String): Message {
        val channel = logic.channel(channelType, channelId)
        val newMessage = message.copy().apply {
            if (id.isEmpty()) {
                id = generateMessageId()
            }
            if (cid.isEmpty()) {
                enrichWithCid(channel.cid)
            }
            user = requireNotNull(globalState.user.value)

            val (attachmentsToUpload, nonFileAttachments) = attachments.partition { it.upload != null }
            attachmentsToUpload.forEach { attachment ->
                if (attachment.uploadId == null) {
                    attachment.uploadId = generateUploadId()
                }
                attachment.uploadState = Attachment.UploadState.Idle
            }
            nonFileAttachments.forEach { attachment ->
                attachment.uploadState = Attachment.UploadState.Success
            }

            type = getMessageType(message)
            createdLocallyAt = createdAt ?: createdLocallyAt ?: Date()
            syncStatus = when {
                attachmentsToUpload.isNotEmpty() -> SyncStatus.AWAITING_ATTACHMENTS
                globalState.isOnline() -> SyncStatus.IN_PROGRESS
                else -> SyncStatus.SYNC_NEEDED
            }
        }
        // Update flow in channel controller
        channel.upsertMessage(newMessage)
        // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
        // Update flow for currently running queries
        logic.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannel(channel.cid) }
        // we insert early to ensure we don't lose messages
        repos.insertMessage(newMessage)
        repos.updateLastMessageForChannel(newMessage.cid, newMessage)
        return newMessage
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
    private suspend fun retryMessage(message: Message, channelType: String, channelId: String): Result<Message> =
        uploadAttachments(message, channelType, channelId)

    /**
     * Uploads the attachment of this message if there is any pending attachments and return the updated message.
     *
     * @param message [Message] whose attachments are to be uploaded.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    private suspend fun uploadAttachments(message: Message, channelType: String, channelId: String): Result<Message> {
        return when {
            globalState.isOnline() ->
                if (message.hasPendingAttachments()) {
                    waitForAttachmentsToBeSent(message, channelType, channelId)
                } else {
                    Result.success(message.copy(type = Message.TYPE_REGULAR))
                }

            message.hasPendingAttachments() -> {
                // We enqueue attachments upload here if user is offline but an error is returned so message is not sent right away.
                enqueueAttachmentUpload(message, channelType, channelId)
                Result(ChatError("Chat is offline, not sending message with id ${message.id} and text ${message.text}"))
            }

            else -> {
                logger.logI("Chat is offline and there is no pending attachments to upload in message with ${message.id} and text ${message.text}")
                Result(ChatError("Chat is offline, there is no pending attachments to upload in message with id ${message.id} and text ${message.text}"))
            }
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
                repos.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                messageToBeSent = repos.selectMessage(newMessage.id) ?: newMessage.copy(
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
        }
    }

    /**
     * Enqueues attachment upload work.
     */
    private fun enqueueAttachmentUpload(message: Message, channelType: String, channelId: String) {
        UploadAttachmentsAndroidWorker.start(context, channelType, channelId, message.id, networkType)
    }

    /**
     * Cancels all the running job.
     */
    fun cancelJobs() {
        jobsMap.values.forEach { it.cancel() }
    }

    /**
     * Returns a unique message id prefixed with user id.
     */
    private fun generateMessageId(): String {
        return globalState.user.value!!.id + "-" + UUID.randomUUID().toString()
    }
}
