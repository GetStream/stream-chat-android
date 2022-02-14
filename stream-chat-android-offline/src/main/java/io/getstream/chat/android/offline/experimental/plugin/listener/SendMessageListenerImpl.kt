package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.SendMessageListener
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.populateMentions

@ExperimentalStreamChatApi
internal class SendMessageListenerImpl(private val logic: LogicRegistry) : SendMessageListener {
    override suspend fun onMessageSendPrecondition(
        channelType: String,
        channelId: String,
        message: Message,
    ): Result<Unit> {
        val channelLogic = logic.channel(channelType, channelId)
        message.populateMentions(channelLogic.toChannel())

        if (message.replyMessageId != null) {
            channelLogic.replyMessage(null)
        }
        return channelLogic.sendMessage(message)
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
