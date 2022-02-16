package io.getstream.chat.android.offline.message.experimental

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.flatMapSuspend
import io.getstream.chat.android.client.utils.onSuccess
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import io.getstream.chat.android.offline.message.getMessageType
import io.getstream.chat.android.offline.message.hasPendingAttachments
import io.getstream.chat.android.offline.message.isEphemeral
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@ExperimentalStreamChatApi
internal class MessageSendingService(
    private val logicRegistry: LogicRegistry,
    // TODO: Pass StateRegistry instead of channelState and globalState once ChatDomain is removed.
    private val channelState: ChannelState,
    private val globalState: GlobalState,
    private val uploadAttachmentsWorker: UploadAttachmentsWorker,
    private val scope: CoroutineScope,
) {
    private val logger = ChatLogger.get("MessageSendingService")
    private var jobsMap: Map<String, Job> = emptyMap()

    private val channelLogic: ChannelLogic by lazy {
        logicRegistry.channel(
            channelState.channelType,
            channelState.channelId
        )
    }

    internal suspend fun prepareMessage(message: Message): Result<Message> {
        message.populateMentions(channelLogic.toChannel())

        if (message.replyMessageId != null) {
            channelLogic.replyMessage(null)
        }

        return Result.success(
            message.copy().apply {
                if (id.isEmpty()) {
                    id = generateMessageId()
                }
                if (cid.isEmpty()) {
                    enrichWithCid(channelState.cid)
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
                type = getMessageType(this)
                createdLocallyAt = createdAt ?: createdLocallyAt ?: Date()
                syncStatus = when {
                    attachmentsToUpload.isNotEmpty() -> SyncStatus.AWAITING_ATTACHMENTS
                    globalState.isOnline() -> SyncStatus.IN_PROGRESS
                    else -> SyncStatus.SYNC_NEEDED
                }
            }
        ).onSuccess { newMessage ->
            // Update flow in channel controller
            channelLogic.upsertMessage(newMessage)
            // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
            // Update flow for currently running queries
            logicRegistry.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannel(channelState.cid) }
        }.onSuccessSuspend { newMessage ->
            channelLogic.storeMessageLocally(listOf(newMessage))
            channelLogic.updateLastMessageForChannel(newMessage)
        }.flatMapSuspend(::sendMessage)
    }

    internal suspend fun sendMessage(message: Message): Result<Message> {
        return when {
            globalState.isOnline() ->
                if (message.hasPendingAttachments()) {
                    waitForAttachmentsToBeSent(message)
                } else Result.success(message.copy(type = Message.TYPE_REGULAR))

            message.hasPendingAttachments() -> {
                enqueueAttachmentUpload(message)
                Result.success(message.copy(type = Message.TYPE_REGULAR))
            }

            else -> {
                logger.logI("Chat is offline, not sending message with id ${message.id} and text ${message.text}")
                Result(ChatError("Chat is offline, not sending message with id ${message.id} and text ${message.text}"))
            }
        }
    }

    private suspend fun waitForAttachmentsToBeSent(newMessage: Message): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        var allAttachmentsUploaded = false
        var messageToBeSent = newMessage
        jobsMap = jobsMap + (
            newMessage.id to scope.launch {
                val ephemeralUploadStatusMessage: Message? = if (newMessage.isEphemeral()) newMessage else null
                channelLogic.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                ephemeralUploadStatusMessage?.let { channelLogic.cancelEphemeralMessage(it) }
                                allAttachmentsUploaded = true
                                messageToBeSent = channelLogic.selectMessage(newMessage.id) ?: newMessage.copy(
                                    attachments = attachments.toMutableList()
                                )
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

    private fun enqueueAttachmentUpload(message: Message) {
        uploadAttachmentsWorker.enqueueJob(
            channelState.channelType,
            channelState.channelId,
            message.id
        )
    }

    private fun generateMessageId(): String {
        return globalState.user.value!!.id + "-" + UUID.randomUUID().toString()
    }

    fun cancelJobs() {
        jobsMap.values.forEach { it.cancel() }
    }
}
