package io.getstream.chat.android.offline.message

import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.flatMapSuspend
import io.getstream.chat.android.client.utils.mapSuspend
import io.getstream.chat.android.client.utils.onSuccess
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.client.utils.recoverSuspend
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date

internal class MessageSendingService(
    private val domainImpl: ChatDomainImpl,
    private val channelController: ChannelController,
    private val channelClient: ChannelClient,
    private val uploadAttachmentsWorker: UploadAttachmentsWorker,
) {
    private val logger = ChatLogger.get("MessageSendingService")
    private var jobsMap: Map<String, Job> = emptyMap()

    internal suspend fun sendNewMessage(message: Message): Result<Message> {
        return Result.success(
            message.copy().apply {
                if (id.isEmpty()) {
                    id = domainImpl.generateMessageId()
                }
                if (cid.isEmpty()) {
                    enrichWithCid(channelController.cid)
                }
                user = requireNotNull(domainImpl.user.value)

                val (attachmentsToUpload, nonFileAttachments) = attachments.partition { it.upload != null }
                attachmentsToUpload.forEach { attachment ->
                    attachment.uploadId = generateUploadId()
                    attachment.uploadState = Attachment.UploadState.Idle
                }
                nonFileAttachments.forEach { attachment ->
                    attachment.uploadState = Attachment.UploadState.Success
                }

                type = getMessageType(message)
                createdLocallyAt = createdAt ?: createdLocallyAt ?: Date()
                syncStatus = when {
                    attachmentsToUpload.isNotEmpty() -> SyncStatus.AWAITING_ATTACHMENTS
                    domainImpl.isOnline() -> SyncStatus.IN_PROGRESS
                    else -> SyncStatus.SYNC_NEEDED
                }
            }
        ).onSuccess { newMessage ->
            // Update flow in channel controller
            channelController.upsertMessage(newMessage)
            // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
            // Update flow for currently running queries
            domainImpl.getActiveQueries().forEach { query -> query.refreshChannel(channelController.cid) }
        }.onSuccessSuspend { newMessage ->
            // we insert early to ensure we don't lose messages
            domainImpl.repos.insertMessage(newMessage)
            domainImpl.repos.updateLastMessageForChannel(newMessage.cid, newMessage)
        }.flatMapSuspend(::sendMessage)
    }

    internal suspend fun sendMessage(message: Message): Result<Message> {
        return when {
            domainImpl.online.value ->
                if (message.hasPendingAttachments()) {
                    waitForAttachmentsToBeSent(message)
                } else {
                    doSend(message)
                }

            message.hasPendingAttachments() -> {
                enqueueAttachmentUpload(message)
                Result.success(message)
            }

            else -> {
                logger.logI("Chat is offline, postponing send message with id ${message.id} and text ${message.text}")
                Result(message)
            }
        }
    }

    private fun waitForAttachmentsToBeSent(newMessage: Message): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        jobsMap = jobsMap + (
            newMessage.id to domainImpl.scope.launch {
                val ephemeralUploadStatusMessage: Message? = if (newMessage.isEphemeral()) newMessage else null
                domainImpl.repos.observeAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                ephemeralUploadStatusMessage?.let { channelController.cancelEphemeralMessage(it) }
                                val messageToBeSent = domainImpl.repos.selectMessage(newMessage.id) ?: newMessage.copy(
                                    attachments = attachments.toMutableList()
                                )
                                doSend(messageToBeSent)
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
        return Result.success(newMessage)
    }

    private fun enqueueAttachmentUpload(message: Message) {
        uploadAttachmentsWorker.enqueueJob(
            channelController.channelType,
            channelController.channelId,
            message.id
        )
    }

    private suspend fun doSend(message: Message): Result<Message> {
        val messageToSend = message.copy(type = "regular")
        return Result.success(messageToSend)
            .onSuccess { logger.logI("Starting to send message with id ${it.id} and text ${it.text}") }
            .flatMapSuspend { newMessage ->
                domainImpl.callRetryService().runAndRetry { channelClient.sendMessage(newMessage) }
            }
            .mapSuspend(channelController::handleSendMessageSuccess)
            .recoverSuspend { error -> channelController.handleSendMessageFail(messageToSend, error) }
    }

    fun cancelJobs() {
        jobsMap.values.forEach { it.cancel() }
    }
}
