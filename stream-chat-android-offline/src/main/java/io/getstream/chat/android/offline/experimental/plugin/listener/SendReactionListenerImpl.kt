package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.SendReactionListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.global.GlobalState
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import io.getstream.chat.android.offline.extensions.addMyReaction
import io.getstream.chat.android.offline.extensions.enrichWithDataBeforeSending
import io.getstream.chat.android.offline.extensions.updateSyncStatus
import io.getstream.chat.android.offline.repository.RepositoryFacade
import java.util.Date

/**
 * [SendReactionListener] implementation for [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 * Handles adding reaction offline, updates the database and does the optimistic UI update.
 *
 * @param logic [LogicRegistry]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.experimental.plugin.OfflinePlugin].
 * @param repos [RepositoryFacade] to cache intermediate data and final result.
 */
internal class SendReactionListenerImpl(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
) : SendReactionListener {

    /**
     * A method called before making an API call to send the reaction.
     * Fills the reaction with necessary data, updates reactions' database
     * and runs optimistic update if [cid] is specified.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     */
    override suspend fun onSendReactionRequest(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
    ) {
        val reactionToSend = reaction.enrichWithDataBeforeSending(
            currentUser = currentUser,
            isOnline = globalState.isOnline(),
            enforceUnique = enforceUnique,
        )

        // Update local storage
        if (enforceUnique) {
            // remove all user's reactions to the message
            repos.updateReactionsForMessageByDeletedDate(
                userId = currentUser.id,
                messageId = reactionToSend.messageId,
                deletedAt = Date(),
            )
        }
        repos.insertReaction(reaction = reactionToSend)

        repos.selectMessage(messageId = reactionToSend.messageId)?.copy()?.let { cachedMessage ->
            cachedMessage.addMyReaction(reaction = reactionToSend, enforceUnique = enforceUnique)
            repos.insertMessage(cachedMessage)

            if (cid != null) {
                doOptimisticMessageUpdate(cid = cid, message = cachedMessage)
            }
        }
    }

    /**
     * Updates [ChannelMutableState.messages].
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param message The [Message] to update.
     */
    private fun doOptimisticMessageUpdate(cid: String, message: Message) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        logic.channel(channelType = channelType, channelId = channelId).upsertMessages(listOf(message))
    }

    /**
     * A method called after receiving the response from the send reaction call.
     * Updates reaction's sync status stored in the database based on API result.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param reaction The [Reaction] to send.
     * @param enforceUnique Flag to determine whether the reaction should replace other ones added by the current user.
     * @param currentUser The currently logged in user.
     * @param result The API call result.
     */
    override suspend fun onSendReactionResult(
        cid: String?,
        reaction: Reaction,
        enforceUnique: Boolean,
        currentUser: User,
        result: Result<Reaction>,
    ) {
        repos.selectUserReactionToMessage(
            reactionType = reaction.type,
            messageId = reaction.messageId,
            userId = currentUser.id,
        )
            ?.let { cachedReaction ->
                repos.insertReaction(cachedReaction.updateSyncStatus(result))
            }
    }

    /**
     * Checks if current user is set and reaction contains required data.
     *
     * @param currentUser The currently logged in user.
     * @param reaction The [Reaction] to send.
     */
    override fun onSendReactionPrecondition(currentUser: User?, reaction: Reaction): Result<Unit> {
        return when {
            currentUser == null -> {
                Result.error(ChatError(message = "Current user is null!"))
            }
            reaction.messageId.isBlank() || reaction.type.isBlank() -> {
                Result.error(ChatError(message = "Reaction::messageId and Reaction::type cannot be empty!"))
            }
            else -> {
                Result.success(Unit)
            }
        }
    }
}
