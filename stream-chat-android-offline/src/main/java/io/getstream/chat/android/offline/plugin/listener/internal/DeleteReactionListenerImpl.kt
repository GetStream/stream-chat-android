package io.getstream.chat.android.offline.plugin.listener.internal

import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.DeleteReactionListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.extensions.internal.removeMyReaction
import io.getstream.chat.android.offline.extensions.internal.updateSyncStatus
import io.getstream.chat.android.offline.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.repository.builder.internal.RepositoryFacade
import java.util.Date

/**
 * [DeleteReactionListener] implementation for [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * Handles adding reaction offline, updates the database and does the optimistic UI update.
 *
 * @param logic [LogicRegistry]
 * @param globalState [GlobalState] provided by the [io.getstream.chat.android.offline.plugin.internal.OfflinePlugin].
 * @param repos [RepositoryFacade] to cache intermediate data and final result.
 */
internal class DeleteReactionListenerImpl(
    private val logic: LogicRegistry,
    private val globalState: GlobalState,
    private val repos: RepositoryFacade,
) : DeleteReactionListener {

    /**
     * A method called before making an API call to delete the reaction.
     * Creates the reaction based on [messageId] and [reactionType], updates reactions' database
     * and runs optimistic update if [cid] is specified.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     */
    override suspend fun onDeleteReactionRequest(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
    ) {
        val reaction = Reaction(
            messageId = messageId,
            type = reactionType,
            user = currentUser,
            userId = currentUser.id,
            syncStatus = if (globalState.isOnline()) SyncStatus.IN_PROGRESS else SyncStatus.SYNC_NEEDED,
            deletedAt = Date(),
        )

        repos.insertReaction(reaction)

        repos.selectMessage(messageId = messageId)?.copy()?.let { cachedMessage ->
            cachedMessage.removeMyReaction(reaction)
            repos.insertMessage(cachedMessage)

            if (cid != null) {
                doOptimisticMessageUpdate(cid = cid, message = cachedMessage)
            }
        }
    }

    /**
     * Updates [io.getstream.chat.android.offline.plugin.state.channel.internal.ChannelMutableState.messages].
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param message The [Message] to update.
     */
    private fun doOptimisticMessageUpdate(cid: String, message: Message) {
        val (channelType, channelId) = cid.cidToTypeAndId()
        logic.channel(channelType = channelType, channelId = channelId).upsertMessages(listOf(message))
    }

    /**
     * A method called after receiving the response from the delete reaction call.
     * Updates reaction's sync status stored in the database based on API result.
     *
     * @param cid The full channel id, i.e. "messaging:123".
     * @param messageId The id of the message to which reaction belongs.
     * @param reactionType The type of reaction.
     * @param currentUser The currently logged in user.
     * @param result The API call result.
     */
    override suspend fun onDeleteReactionResult(
        cid: String?,
        messageId: String,
        reactionType: String,
        currentUser: User,
        result: Result<Message>,
    ) {
        repos.selectUserReactionToMessage(reactionType = reactionType, messageId = messageId, userId = currentUser.id)
            ?.let { cachedReaction ->
                repos.insertReaction(cachedReaction.updateSyncStatus(result))
            }
    }

    /**
     * Checks if current user is set.
     *
     * @param currentUser The currently logged in user.
     */
    override fun onDeleteReactionPrecondition(currentUser: User?): Result<Unit> {
        return if (currentUser != null) {
            Result.success(Unit)
        } else {
            Result.error(ChatError(message = "Current user is null!"))
        }
    }
}
