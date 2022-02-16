package io.getstream.chat.android.offline.experimental.plugin.listener

import android.content.Context
import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.populateMentions
import io.getstream.chat.android.offline.message.MessageSendingServiceFactory
import io.getstream.chat.android.offline.repository.RepositoryFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

@ExperimentalStreamChatApi
internal class SendMessageListenerImpl(
    private val context: Context,
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val scope: CoroutineScope,
    private val repos: RepositoryFacade,
    private val messageSendingServiceFactory: MessageSendingServiceFactory = MessageSendingServiceFactory.getOrCreate(),
) : SendMessageListener {

    private val logger = ChatLogger.get("SendMessageListenerImpl")
    private var jobsMap: Map<String, Job> = emptyMap()

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

    override suspend fun onMessageSendRequest(channelType: String, channelId: String, message: Message) {
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
