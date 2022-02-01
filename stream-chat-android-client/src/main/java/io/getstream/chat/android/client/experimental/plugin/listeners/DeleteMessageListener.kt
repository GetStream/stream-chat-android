package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for message deletion.
 */
public interface DeleteMessageListener {
    /**
     * Method called when a request for message edition happens. Use it to update database, update messages in the SDK,
     * update the UI when a message occurs...
     *
     * @param message [Message].
     */
    public suspend fun onMessageDeleteRequest(message: Message)

    /**
     * Method called when a request for message edition return. Use it to update database, update messages or to present
     * an error to the user.
     *
     * @param result the result of the API call.
     */
    public suspend fun onMessageDeleteResult(originalMessage: Message, result: Result<Message>)
}
