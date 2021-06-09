package io.getstream.chat.android.offline.message.attachment

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl

internal class UploadAttachmentsWorker {
    suspend fun uploadAttachmentsForMessage(channelType: String, channelId: String, messageId: String): Result<Unit> {
        return try {
            val domainImpl = (ChatDomain.instance() as ChatDomainImpl)
            val message = domainImpl.repos.selectMessage(messageId)!!
            val attachments = domainImpl.channel(channelType, channelId)
                .uploadAttachments(message)

            if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                Result.success(Unit)
            } else {
                Result.error(ChatError())
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }
}
