package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

@ExperimentalStreamChatApi
public interface SendMessageListener {
    public fun onMessageSendRequest(
        channelType: String,
        channelId: String,
        message: Message,
    ) { }

    public fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    ) { }
}
