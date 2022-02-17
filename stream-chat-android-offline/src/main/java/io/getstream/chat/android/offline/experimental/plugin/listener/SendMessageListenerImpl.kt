package io.getstream.chat.android.offline.experimental.plugin.listener

import android.content.Context
import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.message.MessageSendingService
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope

@ExperimentalStreamChatApi
internal class SendMessageListenerImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
    private val repos: RepositoryFacade,
    private val messageSendingServiceFactory: MessageSendingServiceFactory = MessageSendingServiceFactory.getOrCreate(),
) : SendMessageListener {

    /**
     * We prepare a message with correct state before sending it to API. A message is prepared in the following steps:
     *
     * 1. Mentions in the message is populated first.
     * 2. If this message is being replied to another message, we clear that replied message in state.
     * 3. With [MessageSendingService], all its attachments are uploaded and correct state variables are set.
     * 4. This updated message is sent back to the caller and used in original API call.
     */
    override suspend fun prepareMessage(channelType: String, channelId: String, message: Message): Result<Message> {
        val channel = logic.channel(channelType, channelId)
        message.populateMentions(channel.toChannel())

        if (message.replyMessageId != null) {
            channel.replyMessage(null)
        }
        return messageSendingServiceFactory.getOrCreateService(
            logic,
            globalState,
            channelType,
            channelId,
            scope,
            repos,
            context
        ).prepareNewMessageWithAttachments(message)
    }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val service = messageSendingServiceFactory.getOrCreateService(
            logic,
            globalState,
            channelType,
            channelId,
            scope,
            repos,
            context
        )

        if (result.isSuccess) {
            service.handleSendMessageSuccess(result.data())
        } else {
            service.handleSendMessageFail(message, result.error())
        }
    }
}
