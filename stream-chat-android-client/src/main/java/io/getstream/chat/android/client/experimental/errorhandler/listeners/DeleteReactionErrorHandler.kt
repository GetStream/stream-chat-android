package io.getstream.chat.android.client.experimental.errorhandler.listeners

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.deleteReaction] calls.
 */
@ExperimentalStreamChatApi
public interface DeleteReactionErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalError The original error returned by the API.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The replacement for the original result.
     */
    public fun onDeleteReactionError(originalError: ChatError, cid: String?, messageId: String): Result<Message>
}
