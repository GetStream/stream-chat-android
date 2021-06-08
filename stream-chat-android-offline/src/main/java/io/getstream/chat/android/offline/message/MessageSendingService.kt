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
import io.getstream.chat.android.client.utils.onSuccessSuspend
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsAndroidWorker
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date

internal class MessageSendingService private constructor() {
    private val logger = ChatLogger.get("ChatDomain MessageSender")
    private var jobsMap: Map<String, Job> = emptyMap()

    internal suspend fun sendMessage(
        message: Message,
        cid: String,
        domainImpl: ChatDomainImpl,
        channelController: ChannelController,
        channelClient: ChannelClient,
    ): Result<Message> {
        return Result.success(
            message.copy().apply {
                if (id.isEmpty()) {
                    id = domainImpl.generateMessageId()
                }
                if (cid.isEmpty()) {
                    enrichWithCid(cid)
                }
                user = requireNotNull(domainImpl.user.value)
                attachments.forEach { attachment ->
                    attachment.uploadId = generateUploadId()
                    attachment.uploadState = Attachment.UploadState.InProgress
                }
                type = getMessageType(message)
                createdLocallyAt = createdAt ?: createdLocallyAt ?: Date()
                syncStatus =
                    if (hasAttachments()) SyncStatus.WAIT_ATTACHMENTS else if (domainImpl.isOnline()) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED
            }
        ).onSuccess { newMessage ->
            // Update flow in channel controller
            channelController.upsertMessage(newMessage)
            // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
            // Update flow for currently running queries
            domainImpl.getActiveQueries().forEach { query -> query.refreshChannel(cid) }
        }.onSuccessSuspend { newMessage ->
            // we insert early to ensure we don't lose messages
            domainImpl.repos.insertMessage(newMessage)
            domainImpl.repos.updateLastMessageForChannel(newMessage.cid, newMessage)
        }.flatMapSuspend { newMessage ->
            send(newMessage, domainImpl, channelClient, channelController)
        }
    }

    private suspend fun send(
        newMessage: Message,
        domainImpl: ChatDomainImpl,
        channelClient: ChannelClient,
        channelController: ChannelController,
    ): Result<Message> {

        return if (domainImpl.online.value) {
            return if (newMessage.hasAttachments()) {
                waitForAttachmentsToBeSent(newMessage, domainImpl, channelClient, channelController)
            } else {
                doSend(newMessage, domainImpl, channelClient, channelController)
            }
        } else {
            logger.logI("Chat is offline, postponing send message with id ${newMessage.id} and text ${newMessage.text}")
            Result(newMessage)
        }
    }

    private fun waitForAttachmentsToBeSent(
        newMessage: Message,
        domainImpl: ChatDomainImpl,
        channelClient: ChannelClient,
        channelController: ChannelController,
    ): Result<Message> {
        jobsMap[newMessage.id]?.cancel()
        logger.logW("Waiting for attachment for message with id ${newMessage.id}")
        jobsMap = jobsMap + (
            newMessage.id to domainImpl.scope.launch {
                val ephemeralUploadStatusMessage: Message? = if (newMessage.isEphemeral()) newMessage else null
                domainImpl.repos.observerAttachmentsForMessage(newMessage.id)
                    .distinctUntilChanged()
                    .filterNot(Collection<Attachment>::isEmpty)
                    .collect { attachments ->
                        when {
                            attachments.all { it.uploadState == Attachment.UploadState.Success } -> {
                                ephemeralUploadStatusMessage?.let { channelController.cancelEphemeralMessage(it) }
                                logger.logW("Attachments are sent. Starting do send message ${newMessage.id}")
                                doSend(newMessage, domainImpl, channelClient, channelController)
                                logger.logW("Message is sent ${newMessage.id}")
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

    private suspend fun doSend(
        message: Message,
        domainImpl: ChatDomainImpl,
        channelClient: ChannelClient,
        channelController: ChannelController,
    ): Result<Message> {
        val messageToSend = message.copy(type = "regular")
        return Result.success(messageToSend)
            .onSuccess { logger.logW("Starting to send message with id ${it.id} and text ${it.text}") }
            .flatMapSuspend { newMessage -> domainImpl.runAndRetry { channelClient.sendMessage(newMessage) } }
            .mapSuspend(channelController::handleSendMessageSuccess)
            .mapErrorSuspend { error -> channelController.handleSendMessageFail(messageToSend, error) }
    }

    internal companion object {
        private var instance: MessageSendingService? = null

        internal fun instance(): MessageSendingService {
            if (instance == null) {
                instance = MessageSendingService()
            }
            return instance!!
        }
    }
}
