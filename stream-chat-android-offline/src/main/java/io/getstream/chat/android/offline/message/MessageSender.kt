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
import io.getstream.chat.android.client.utils.mapErrorSuspend
import io.getstream.chat.android.client.utils.mapSuspend
import io.getstream.chat.android.client.utils.onSuccess
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsAndroidWorker
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date

internal class MessageSender(
    private val cid: String,
    private val domainImpl: ChatDomainImpl,
    private val channelController: ChannelController,
    private val channelClient: ChannelClient,
) {

    private val logger = ChatLogger.get("ChatDomain MessageSender")
    private var jobsMap: Map<String, Job> = emptyMap()

    internal suspend fun sendMessage(message: Message): Result<Message> {
        val newMessage = message.copy()

        // set defaults for id, cid and created at
        if (newMessage.id.isEmpty()) {
            newMessage.id = domainImpl.generateMessageId()
        }
        if (newMessage.cid.isEmpty()) {
            newMessage.enrichWithCid(cid)
        }

        newMessage.user = requireNotNull(domainImpl.user.value)

        newMessage.attachments.forEach { attachment ->
            attachment.uploadId = generateUploadId()
            attachment.uploadState = Attachment.UploadState.InProgress
        }

        newMessage.type = getMessageType(message)
        newMessage.createdLocallyAt = newMessage.createdAt ?: newMessage.createdLocallyAt ?: Date()
        newMessage.syncStatus =
            if (newMessage.hasAttachments()) SyncStatus.WAIT_ATTACHMENTS else if (domainImpl.isOnline()) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED

        // Update flow in channel controller
        channelController.upsertMessage(newMessage)
        // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
        // Update flow for currently running queries
        for (query in domainImpl.getActiveQueries()) {
            query.refreshChannel(cid)
        }

        // we insert early to ensure we don't lose messages
        domainImpl.repos.insertMessage(newMessage)
        domainImpl.repos.updateLastMessageForChannel(newMessage.cid, newMessage)

        return send(newMessage)
    }

    private suspend fun send(newMessage: Message): Result<Message> {
        return if (domainImpl.online.value) {
            return if (newMessage.hasAttachments()) {
                waitForAttachmentsToBeSent(newMessage)
            } else {
                doSend(newMessage)
            }
        } else {
            logger.logI("Chat is offline, postponing send message with id ${newMessage.id} and text ${newMessage.text}")
            Result(newMessage)
        }
    }

    private fun waitForAttachmentsToBeSent(newMessage: Message): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        jobsMap = jobsMap + (
            newMessage.id to domainImpl.scope.launch {
                val ephemeralUploadStatusMessage: Message? = if (newMessage.isEphemeral()) newMessage else null
                domainImpl.repos.observerAttachmentsForMessage(newMessage.id)
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                ephemeralUploadStatusMessage?.let { channelController.cancelEphemeralMessage(it) }
                                doSend(newMessage)
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
        UploadAttachmentsAndroidWorker.start(
            domainImpl.appContext,
            channelController.channelType,
            channelController.channelId,
            newMessage.id
        )
        return Result.success(newMessage)
    }

    private suspend fun doSend(message: Message): Result<Message> {
        val messageToSend = message.copy(type = "regular")
        return Result.success(messageToSend)
            .onSuccess { newMessage -> logger.logI("Starting to send message with id ${newMessage.id} and text ${newMessage.text}") }
            .flatMapSuspend { newMessage -> domainImpl.runAndRetry { channelClient.sendMessage(newMessage) } }
            .mapSuspend(channelController::handleSendMessageSuccess)
            .mapErrorSuspend { error -> channelController.handleSendMessageFail(messageToSend, error) }
    }
}
