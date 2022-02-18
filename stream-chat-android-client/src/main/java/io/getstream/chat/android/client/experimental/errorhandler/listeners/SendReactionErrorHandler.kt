package io.getstream.chat.android.client.experimental.errorhandler.listeners

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result

/**
 * Error handler for [io.getstream.chat.android.client.ChatClient.sendReaction] calls.
 */
public interface SendReactionErrorHandler {

    /**
     * Returns a [Result] from this side effect when original request is failed.
     *
     * @param originalError The original error returned by the API.
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @return result The replacement for the original result.
     */
    public fun onSendReactionError(
        originalError: ChatError,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ): Result<Reaction>
}
