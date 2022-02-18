package io.getstream.chat.android.offline.experimental.errorhandler.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.errorhandler.listeners.SendReactionErrorHandler
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.extensions.enrichWithDataBeforeSending

/**
 * [SendReactionErrorHandler] implementation for [io.getstream.chat.android.offline.experimental.errorhandler.OfflineErrorHandler].
 * Returns a [Reaction] instance enriched with user [Reaction.syncStatus] if reaction was send offline and can be synced.
 *
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 */
@ExperimentalStreamChatApi
internal class SendReactionErrorHandlerImpl(private val globalState: GlobalState) : SendReactionErrorHandler {

    /**
     * Replaces the original response error if the user is offline.
     * This means that the reaction was added locally but the API request failed due to lack of connection.
     * The request will be synced once user's connection is recovered.
     *
     * @param originalError The original error returned by the API.
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     *
     * @return result The original or offline related result.
     */
    override fun onSendReactionError(
        originalError: ChatError,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ): Result<Reaction> {
        return if (globalState.isOnline()) {
            Result.error(originalError)
        } else {
            Result.success(
                reaction.enrichWithDataBeforeSending(
                    currentUser = currentUser,
                    isOnline = globalState.isOnline(),
                    enforceUnique = enforceUnique,
                ),
            )
        }
    }
}
