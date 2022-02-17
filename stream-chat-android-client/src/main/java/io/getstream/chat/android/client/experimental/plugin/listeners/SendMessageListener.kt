package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Listener for [ChatClient.sendMessage] requests.
 */
@ExperimentalStreamChatApi
public interface SendMessageListener {

    /**
     * Prepares the message with proper attachments, type and cid before sending it to the API.
     *
     * @param channelType The type of the channel in which message is sent.
     * @param channelId The id of the the channel in which message is sent.
     * @param message Message to be sent.
     *
     * @return [Result] of [Message] with uploaded attachments (if any), correct type and timestamp.
     */
    public suspend fun prepareMessage(
        channelType: String,
        channelId: String,
        message: Message
    ): Result<Message>

    /**
     * Side effect to be invoked when the original request is completed with a response.
     *
     * @param result [Result] response from the original request.
     * @param channelType The type of the channel in which message is sent.
     * @param channelId The id of the the channel in which message is sent.
     * @param message Message to be sent.
     */
    public suspend fun onMessageSendResult(
        result: Result<Message>,
        channelType: String,
        channelId: String,
        message: Message,
    )
}
