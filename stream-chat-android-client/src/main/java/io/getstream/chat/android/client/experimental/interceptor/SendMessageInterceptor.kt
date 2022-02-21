package io.getstream.chat.android.client.experimental.interceptor

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Intercepts the outgoing requests and potentially modifies the Message being sent to the API.
 */
@InternalStreamChatApi
public interface SendMessageInterceptor {
    /**
     * Intercept the message before sending it to the API.
     *
     * @param channelType The type of the channel in which message is sent.
     * @param channelId The id of the the channel in which message is sent.
     * @param message Message to be sent.
     *
     * @return [Result] of [Message] after intercepting.
     */
    public suspend fun interceptMessage(
        channelType: String,
        channelId: String,
        message: Message,
    ): Result<Message>
}
