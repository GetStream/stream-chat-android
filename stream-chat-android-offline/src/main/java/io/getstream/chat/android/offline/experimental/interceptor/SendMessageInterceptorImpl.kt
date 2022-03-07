package io.getstream.chat.android.offline.experimental.interceptor

import android.content.Context
import io.getstream.chat.android.client.experimental.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory

/**
 * Implementation of [SendMessageInterceptor] that upload attachments, update original message
 * with new attachments and return updated message.
 */
internal class SendMessageInterceptorImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val messageSendingService: MessageSendingServiceFactory,
) : SendMessageInterceptor {

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

        val messageSendingService = messageSendingService.getOrCreateService(context, channelType, channelId)
        return if (!isRetrying) messageSendingService.prepareNewMessageWithAttachments(message)
        else messageSendingService.retryMessage(message)
    }
}
