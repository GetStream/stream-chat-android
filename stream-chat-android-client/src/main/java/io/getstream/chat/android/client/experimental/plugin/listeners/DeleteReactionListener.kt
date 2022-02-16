package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.deleteReaction] calls.
 */
@ExperimentalStreamChatApi
public interface DeleteReactionListener {

    /**
     * A method called before making an API call to delete the reaction.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     */
    public suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    )

    /**
     * A method called after receiving the response from the delete reaction call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     * @param result The API call result.
     */
    public suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    )

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

    /**
     * Runs precondition check for [ChatClient.deleteReaction].
     * The request will be run if the method returns [Result.success] and won't be made if it returns [Result.error].
     *
     * @param currentUser The currently logged in user.
     *
     * @return [Result.success] if the precondition is fulfilled, [Result.error] otherwise.
     */
    public fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit>
}
