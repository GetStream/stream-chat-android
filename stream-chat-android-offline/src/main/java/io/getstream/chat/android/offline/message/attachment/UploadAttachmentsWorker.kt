package io.getstream.chat.android.offline.message.attachment

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomainImpl

internal class UploadAttachmentsWorker(private val appContext: Context) {
    suspend fun uploadAttachmentsForMessage(
        channelType: String,
        channelId: String,
        messageId: String,
        chatDomain: ChatDomainImpl,
        chatClient: ChatClient
    ): Result<Unit> {
        return try {
            val domainImpl = chatDomain.apply {
                if (!isRepositoryInitialized) {
                    chatClient.setUserWithoutConnectingIfNeeded()

                    chatClient.getCurrentUser()?.let(::initRepositoryFacade)
                }
            }

            val message = domainImpl.repos.selectMessage(messageId)!!

            val hasPendingAttachment = message.attachments.any { attachment ->
                attachment.uploadState == Attachment.UploadState.InProgress
            }

            if (!hasPendingAttachment) {
                return Result.success(Unit)
            }

            val attachments = domainImpl.channel(channelType, channelId).uploadAttachments(message)

            if (attachments.all { it.uploadState == Attachment.UploadState.Success }) {
                Result.success(Unit)
            } else {
                Result.error(ChatError())
            }
        } catch (e: Exception) {
            Result.error(e)
        }
    }

    fun enqueueJob(
        channelType: String,
        channelId: String,
        messageId: String,
    ) {
        UploadAttachmentsAndroidWorker.start(appContext, channelType, channelId, messageId)
    }
}
