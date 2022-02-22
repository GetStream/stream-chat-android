package io.getstream.chat.android.offline.message

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.isPermanent
import io.getstream.chat.android.client.extensions.retry
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.flatMapSuspend
import io.getstream.chat.android.client.utils.mapSuspend
import io.getstream.chat.android.client.utils.recoverSuspend
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

/**
 * A service that does everything required to send message i.e. preparing the message, uploading attachments, setting correct state.
 */
@ExperimentalStreamChatApi
internal class MessageSendingService(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val channelType: String,
    private val channelId: String,
    private val scope: CoroutineScope,
    private val repos: RepositoryFacade,
    private val uploadAttachmentsWorker: UploadAttachmentsWorker,
    // TODO: This should be removed once ChatDomain is merged.
    private val chatClient: ChatClient = ChatClient.instance(),
) {
    private val logger = ChatLogger.get("MessageSendingService")
    private var jobsMap: Map<String, Job> = emptyMap()

    private val channel by lazy { logic.channel(channelType, channelId) }

    /**
     * Prepares message and upload its attachments if it has any.
     *
     * @param message [Message] to be sent.
     *
     * @return [Result] with a prepared message.
     */
    suspend fun prepareNewMessageWithAttachments(message: Message): Result<Message> {
        val preparedMessage = prepareNewMessage(message)
        return uploadAttachments(preparedMessage)
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
    private suspend fun prepareNewMessage(message: Message): Message {
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
     * Sends a new message after preparing and uploading the attachments.
     *
     * @param message Message to be sent.
     *
     * @return Result with a newly updated message.
     */
    internal suspend fun sendNewMessage(message: Message): Result<Message> {
        val preparedMessage = prepareNewMessage(message)
        return sendMessage(preparedMessage)
    }

    /**
     * Sends the message without preparing.
     *
     * It is used when we have some messages already pending in database (due to any non permanent error)
     *
     * TODO: This method can be removed when ChatDomain is removed.
     */
    internal suspend fun sendMessage(message: Message): Result<Message> {
        return uploadAttachments(message)
            .flatMapSuspend { doSend(it) }
            .recoverSuspend { message }
    }

    /**
     * Uploads the attachment of this message if there is any pending attachments and return the updated message.
     *
     * @param message [Message] whose attachments are to be uploaded.
     *
     * @return [Result] having message with latest attachments state or error if there was any.
     */
    private suspend fun uploadAttachments(message: Message): Result<Message> {
        return when {
            globalState.isOnline() ->
                if (message.hasPendingAttachments()) {
                    waitForAttachmentsToBeSent(message)
                } else Result.success(message.copy(type = Message.TYPE_REGULAR))

            message.hasPendingAttachments() -> {
                // We enqueue attachments upload here if user is offline but an error is returned so message is not sent right away.
                enqueueAttachmentUpload(message)
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
    private suspend fun waitForAttachmentsToBeSent(newMessage: Message): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        var allAttachmentsUploaded = false
        var messageToBeSent = newMessage
        jobsMap = jobsMap + (
            newMessage.id to scope.launch {
                val ephemeralUploadStatusMessage: Message? = if (newMessage.isEphemeral()) newMessage else null
                repos.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                ephemeralUploadStatusMessage?.let {
                                    cancelEphemeralMessage(it)
                                }
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
        enqueueAttachmentUpload(newMessage)
        jobsMap[newMessage.id]?.join()
        return if (allAttachmentsUploaded) {
            Result.success(messageToBeSent.copy(type = Message.TYPE_REGULAR))
        } else Result.error(ChatError("Could not upload attachments, not sending message with id ${newMessage.id}"))
    }

    /**
     * Cancels ephemeral Message.
     * Removes message from the offline storage and memory and notifies about update.
     *
     * @param message [Message] to be cancelled.
     */
    private suspend fun cancelEphemeralMessage(message: Message): Result<Boolean> {
        require(message.isEphemeral()) { "Only ephemeral message can be canceled" }
        repos.deleteChannelMessage(message)
        channel.removeLocalMessage(message)
        return Result(true)
    }

    /**
     * Enqueues attachment upload work.
     */
    private fun enqueueAttachmentUpload(message: Message) {
        uploadAttachmentsWorker.enqueueJob(channelType, channelId, message.id)
    }

    // TODO: Remove this method when ChatDomain is removed.
    private suspend fun doSend(message: Message): Result<Message> =
        chatClient.channel(message.cid).sendMessageInternal(message)
            .retry(scope, chatClient.retryPolicy).await()
            .mapSuspend(::handleSendMessageSuccess)
            .recoverSuspend { error -> handleSendMessageFail(message, error) }

    /**
     * Updates the message object and local database with new message state after message is sent successfully.
     *
     * @param processedMessage [Message] returned from API response.
     * @return [Message] Updated message.
     */
    internal suspend fun handleSendMessageSuccess(processedMessage: Message): Message {
        // Don't update latest message with this id if it is already synced.
        val latestUpdatedMessage = repos.selectMessage(processedMessage.id) ?: processedMessage
        if (latestUpdatedMessage.syncStatus == SyncStatus.COMPLETED) {
            return latestUpdatedMessage
        }
        return latestUpdatedMessage.enrichWithCid(channel.cid)
            .copy(syncStatus = SyncStatus.COMPLETED)
            .also {
                repos.insertMessage(it)
                channel.upsertMessage(it)
            }
    }

    /**
     * Updates the message object and local database with new message state after message wasn't sent due to error.
     *
     * @param message [Message] that were being sent.
     * @param error [ChatError] with the reason of failure.
     *
     * @return [Message] Updated message.
     */
    internal suspend fun handleSendMessageFail(message: Message, error: ChatError): Message {
        logger.logE(
            "Failed to send message with id ${message.id} and text ${message.text}: $error",
            error
        )
        // Don't update latest message with this id if it is already synced.
        val latestUpdatedMessage = repos.selectMessage(message.id) ?: message
        if (latestUpdatedMessage.syncStatus == SyncStatus.COMPLETED) {
            return latestUpdatedMessage
        }
        return message.copy(
            syncStatus = if (error.isPermanent()) {
                SyncStatus.FAILED_PERMANENTLY
            } else {
                SyncStatus.SYNC_NEEDED
            },
            updatedLocallyAt = Date(),
        )
            .also {
                repos.insertMessage(it)
                channel.upsertMessage(it)
            }
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
