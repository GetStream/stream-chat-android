package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.message.experimental.MessageSendingServiceFactory

@ExperimentalStreamChatApi
internal class SendMessageListenerImpl(
    private val logic: LogicRegistry,
    private val messageSendingServiceFactory: MessageSendingServiceFactory,
) : SendMessageListener {

    override suspend fun prepareMessage(channelType: String, channelId: String, message: Message): Result<Message> {
        return messageSendingServiceFactory.getOrCreate(channelType, channelId).prepareMessage(message)
    }

    override suspend fun onMessageSendRequest(channelType: String, channelId: String, message: Message) {
    }

    override suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) {
        val channelLogic = logic.channel(channelType, channelId)
        if (result.isSuccess) {
            channelLogic.handleSendMessageSuccess(result.data())
        } else {
            channelLogic.handleSendMessageFail(message, result.error())
        }
    }
}
