package io.getstream.chat.android.client.persistance.repository

import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.utils.SyncStatus
import java.util.Date

public interface ReactionRepository {
    public suspend fun insertReaction(reaction: Reaction)
    public suspend fun updateReactionsForMessageByDeletedDate(userId: String, messageId: String, deletedAt: Date)
    public suspend fun selectReactionsBySyncStatus(syncStatus: SyncStatus): List<Reaction>
    /**
     * Selects the reaction of given type to the message if exists.
     *
     * @param reactionType The type of reaction.
     * @param messageId The id of the message to which reaction belongs.
     * @param userId The id of the user who is the owner of reaction.
     *
     * @return [Reaction] if exists, null otherwise.
     */
    public suspend fun selectUserReactionToMessage(reactionType: String, messageId: String, userId: String): Reaction?

    public suspend fun selectUserReactionsToMessage(
        messageId: String,
        userId: String,
    ): List<Reaction>
}
