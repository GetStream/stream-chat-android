package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Listener for [io.getstream.chat.android.client.ChatClient.sendReaction] calls.
 */
@ExperimentalStreamChatApi
public interface SendReactionListener {

    /**
     * A method called before making an API call to send the reaction.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     */
    public suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    )

    /**
     * A method called after receiving the response from the send reaction call.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @param result The API call result.
     */
    public suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    )

    /**
     * Runs precondition check for [ChatClient.sendReaction].
     * The request will be run if the method returns [Result.success] and won't be made if it returns [Result.error].
     *
     * @param currentUser The currently logged in user.
     * @param reaction The [Reaction] to send.
     *
     * @return [Result.success] if the precondition is fulfilled, [Result.error] otherwise.
     */
    public fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit>
}
