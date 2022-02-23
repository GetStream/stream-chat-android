package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result

/**
 * Listener for requests of message deletion and for message deletion results.
 */
public interface DeleteMessageListener {

    /**
     * Method called when a request to delete a message in the API happens
     *
     * @param messageId
     */
    public suspend fun onMessageDeleteRequest(messageId: String)

    /**
     * Method called when a request for message deletion return. Use it to update database, update messages or to present
     * an error to the user.
     *
     * @param result the result of the API call.
     */
    public suspend fun onMessageDeleteResult(originalMessageId: String, result: Result<Message>)
}
