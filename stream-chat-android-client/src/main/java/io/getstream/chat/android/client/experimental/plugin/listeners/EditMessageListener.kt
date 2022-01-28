package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for editions in messages
 */
public interface EditMessageListener {

    /**
     * Method called when an edition in a message starts happens
     *
     * @param message [Message]
     */
    public suspend fun onMessageEditRequest(message: Message)

    /**
     * Method called when an edition in a message returns from the API
     *
     * @param result the result of the API call
     */
    public fun onMessageEditResult(result: Result<Message>)
}
