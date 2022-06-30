package io.getstream.chat.android.client.interceptor.message

import io.getstream.chat.android.client.extensions.enrichWithCid
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.network.NetworkStateProvider
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.client.utils.internal.getMessageType
import java.util.Date
import java.util.UUID

internal class PrepareMessageInterceptorImpl(
    private val networkStateProvider: NetworkStateProvider,
) : PrepareMessageInterceptor {

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
    override fun prepareMessage(message: Message, channelId: String, channelType: String, userId: String): Message {
        return message.copy().apply {
            if (id.isEmpty()) {
                id = generateMessageId(userId)
            }
            if (cid.isEmpty()) {
                enrichWithCid("$channelType:$channelId")
            }

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
                networkStateProvider.isConnected() -> SyncStatus.IN_PROGRESS
                else -> SyncStatus.SYNC_NEEDED
            }
        }
    }

    /**
     * Returns a unique message id prefixed with user id.
     */
    private fun generateMessageId(userid: String): String {
        return "$userid-${UUID.randomUUID()}"
    }

    private fun generateUploadId(): String {
        return "upload_id_${UUID.randomUUID()}"
    }
}
