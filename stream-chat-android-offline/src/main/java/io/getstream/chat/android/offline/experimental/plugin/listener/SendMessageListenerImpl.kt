package io.getstream.chat.android.offline.experimental.plugin.listener

import android.content.Context
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.isPermanent
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
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.message.attachment.UploadAttachmentsWorker
import io.getstream.chat.android.offline.message.attachment.generateUploadId
import io.getstream.chat.android.offline.message.getMessageType
import io.getstream.chat.android.offline.message.hasPendingAttachments
import io.getstream.chat.android.offline.message.isEphemeral
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

@ExperimentalStreamChatApi
internal class SendMessageListenerImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
    private val repos: RepositoryFacade,
) : SendMessageListener {

    private val logger = ChatLogger.get("SendMessageListenerImpl")
    private var jobsMap: Map<String, Job> = emptyMap()
    private val uploadAttachmentsWorker = UploadAttachmentsWorker(context)

    override suspend fun prepareMessage(channelType: String, channelId: String, message: Message): Result<Message> {
        val channel = logic.channel(channelType, channelId)
        message.populateMentions(channel.toChannel())

        if (message.replyMessageId != null) {
            channel.replyMessage(null)
        }
        return Result.success(
            message.copy().apply {
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
            channel.upsertMessage(newMessage)
            // TODO: an event broadcasting feature for LOCAL/offline events on the LLC would be a cleaner approach
            // Update flow for currently running queries
            logic.getActiveQueryChannelsLogic().forEach { query -> query.refreshChannel(channel.cid) }
        }.onSuccessSuspend { newMessage ->
            channel.storeMessageLocally(listOf(newMessage))
            repos.updateLastMessageForChannel(channel.cid, message)
        }.flatMapSuspend { newMessage ->
            sendMessage(channelType, channelId, newMessage)
        }
    }

    internal suspend fun sendMessage(channelType: String, channelId: String, message: Message): Result<Message> {
        return when {
            globalState.isOnline() ->
                if (message.hasPendingAttachments()) {
                    waitForAttachmentsToBeSent(channelType, channelId, message)
                } else Result.success(message.copy(type = Message.TYPE_REGULAR))

            message.hasPendingAttachments() -> {
                enqueueAttachmentUpload(channelType, channelId, message)
                Result.success(message.copy(type = Message.TYPE_REGULAR))
            }

            else -> {
                logger.logI("Chat is offline, not sending message with id ${message.id} and text ${message.text}")
                Result(ChatError("Chat is offline, not sending message with id ${message.id} and text ${message.text}"))
            }
        }
    }

    private suspend fun waitForAttachmentsToBeSent(
        channelType: String,
        channelId: String,
        newMessage: Message,
    ): Result<Message> {
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
                                    cancelEphemeralMessage(
                                        logic.channel(
                                            channelType,
                                            channelId
                                        ),
                                        it
                                    )
                                }
                                allAttachmentsUploaded = true
                                messageToBeSent = repos.selectMessage(newMessage.id) ?: newMessage.copy(
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
        enqueueAttachmentUpload(channelType, channelId, newMessage)
        jobsMap[newMessage.id]?.join()
        return if (allAttachmentsUploaded) {
            Result.success(messageToBeSent.copy(type = Message.TYPE_REGULAR))
        } else Result.error(ChatError("Could not upload attachments, not sending message with id ${newMessage.id}"))
    }

    private fun enqueueAttachmentUpload(channelType: String, channelId: String, message: Message) {
        uploadAttachmentsWorker.enqueueJob(channelType, channelId, message.id)
    }

    private fun generateMessageId(): String {
        return globalState.user.value!!.id + "-" + UUID.randomUUID().toString()
    }

    /**
     * Cancels ephemeral Message.
     * Removes message from the offline storage and memory and notifies about update.
     */
    private suspend fun cancelEphemeralMessage(channel: ChannelLogic, message: Message): Result<Boolean> {
        require(message.isEphemeral()) { "Only ephemeral message can be canceled" }
        repos.deleteChannelMessage(message)
        channel.removeLocalMessage(message)
        return Result(true)
    }

    override suspend fun onMessageSendRequest(channelType: String, channelId: String, message: Message) {
    }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val channel = logic.channel(channelType, channelId)

        if (result.isSuccess) {
            result.data()
                .enrichWithCid(cid = channel.cid)
                .copy(syncStatus = SyncStatus.COMPLETED)
        } else {
            message
                .copy(
                    syncStatus = if (result.error().isPermanent()) {
                        SyncStatus.FAILED_PERMANENTLY
                    } else {
                        SyncStatus.SYNC_NEEDED
                    },
                    updatedLocallyAt = Date(),
                )
        }.also {
            repos.insertMessage(it)
            channel.upsertMessage(it)
        }
    }
}
