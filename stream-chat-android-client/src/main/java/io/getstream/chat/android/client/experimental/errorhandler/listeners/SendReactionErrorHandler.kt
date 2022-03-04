package io.getstream.chat.android.client.experimental.errorhandler.listeners

import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.call.ReturnOnErrorCall
import io.getstream.chat.android.client.experimental.errorhandler.ErrorHandler
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.sendReaction] calls.
 */
public interface SendReactionErrorHandler : ErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalCall The original call.
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @return result The replacement for the original result.
     */
    public fun onSendReactionError(
        originalCall: Call<Reaction>,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ): ReturnOnErrorCall<Reaction>
}

internal fun Call<Reaction>.onReactionError(
    errorHandlers: List<SendReactionErrorHandler>,
    reaction: Reaction,
    enforceUnique: Boolean,
    currentUser: User,
): Call<Reaction> {
    return errorHandlers.fold(this) { originalCall, errorHandler ->
        errorHandler.onSendReactionError(originalCall, reaction, enforceUnique, currentUser)
    }
}
