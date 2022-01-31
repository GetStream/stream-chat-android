package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for editions in messages
 */
public interface EditMessageListener {

    /**
     * Method called when a message edit request happens.
     *
     * @param message [Message].
     */
    public suspend fun onMessageEditRequest(message: Message)

    /**
     * Method called when an edition request returns from the API.
     *
     * @param result the result of the API call.
     */
    public suspend fun onMessageEditResult(originalMessage: Message, result: Result<Message>)
}
