package io.getstream.chat.android.client.experimental.errorhandler.listeners

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.deleteReaction] calls.
 */
@ExperimentalStreamChatApi
public interface DeleteReactionErrorHandlerProposal : ErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalError The original error returned by the API.
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     *
     * @return result The replacement for the original result.
     */
    public fun onDeleteReactionError(
        originalCall: Call<Message>,
        cid: String?,
        messageId: String,
    ): ReturnOnErrorCall<Message>
}

@ExperimentalStreamChatApi
public fun Call<Message>.onMessageError(
    errorHandlers: List<DeleteReactionErrorHandlerProposal>,
    cid: String?,
    messageId: String,
): Call<Message> {
    return errorHandlers.fold(this) { messageCall, errorHandler ->
        errorHandler.onDeleteReactionError(messageCall, cid, messageId)
    }
}
