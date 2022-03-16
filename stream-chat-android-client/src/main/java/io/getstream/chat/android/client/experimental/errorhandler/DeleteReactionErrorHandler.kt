package io.getstream.chat.android.client.experimental.errorhandler

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.deleteReaction] calls.
 */
@InternalStreamChatApi
public interface DeleteReactionErrorHandler : ErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalCall The original call.
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

internal fun Call<Message>.onMessageError(
    errorHandlers: List<DeleteReactionErrorHandler>,
    cid: String?,
    messageId: String,
): Call<Message> {
    return errorHandlers.fold(this) { messageCall, errorHandler ->
        errorHandler.onDeleteReactionError(messageCall, cid, messageId)
    }
}
