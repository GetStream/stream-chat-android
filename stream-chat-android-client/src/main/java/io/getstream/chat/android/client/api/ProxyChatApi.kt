package io.getstream.chat.android.client.api

import io.getstream.chat.android.client.sender.MessageSender
import io.getstream.chat.android.models.Message
import io.getstream.result.call.Call
import io.getstream.result.call.CoroutineCall
import kotlinx.coroutines.CoroutineScope

internal class ProxyChatApi(
    private val delegate: ChatApi,
    private val scope: CoroutineScope,
    private val messageSender: MessageSender?,
): ChatApi by delegate {

    override fun sendMessage(channelType: String, channelId: String, message: Message): Call<Message> {
        return messageSender?.let {
            CoroutineCall(scope) {
                it.sendMessage(channelType, channelId, message)
            }
        } ?: delegate.sendMessage(channelType, channelId, message)
    }
}